# API MinIO (demo) — Upload và Preview

Base URL mặc định khi chạy local: `http://localhost:8080`

Hai API chính bạn cần:

| STT | Mục đích | Method | Đường dẫn |
|-----|----------|--------|-----------|
| 1 | Upload file lên MinIO | `POST` | `/api/demo/minio/upload` |
| 2 | Preview / tải tạm qua trình duyệt | `GET` | `/api/demo/minio/preview?key=...` |

Điều kiện để API hoạt động:

- Bật profile `local` (hoặc cấu hình tương đương) và `app.storage.minio.enabled=true`.
- Chạy MinIO (`docker compose up -d minio`), tạo bucket trùng `app.storage.minio.bucket` (mặc định `sale-app-files`). Ở local có thể bật `app.storage.minio.auto-create-bucket=true` để app tự tạo bucket nếu chưa có.
- Hướng dẫn bật MinIO và bucket: `docs/minio-upload.md`.

---

## 1) Upload file

- **URL:** `POST /api/demo/minio/upload`
- **Header:** `Content-Type: multipart/form-data`
- **Field:** `file` (bắt buộc) — có thể là bất kỳ loại file nào; kích thước tối đa theo cấu hình Spring (`spring.servlet.multipart`, hiện ~25MB).
- **Query (tuỳ chọn):** `folder` (mặc định `documents`). Ví dụ `folder=documents/invoices`.
- **Query (tuỳ chọn):** `documentType` (mặc định `GENERIC_FILE`).

### Ví dụ `curl`

```bash
curl -s -F "file=@/đường/dẫn/ảnh.png" \
  "http://localhost:8080/api/demo/minio/upload?folder=documents&documentType=ID_CARD_FRONT"
```

### Response (JSON)

Ứng dụng trả JSON gồm tối thiểu:

- `documentId` — id bản ghi trong bảng `documents`
- `bucket` — tên bucket trên MinIO
- `folder` — prefix/folder đã dùng khi lưu object
- `path` — đường dẫn file trong object storage (file_path)
- `size` — kích thước file (bytes)
- `contentType` — MIME type (có thể rỗng nếu client không gửi)

Ví dụ:

```json
{
  "documentId": "d6a14f08-4be6-4f5b-9e76-8e4d2f3a4f11",
  "bucket": "sale-app-files",
  "folder": "documents",
  "path": "documents/1745820000000-a1b2c3d4-...-ảnh.png",
  "size": 123456,
  "contentType": "image/png"
}
```

---

## 2) Preview (mở trong trình duyệt)

- **URL:** `GET /api/demo/minio/preview?key=<KEY>`
- **Tham số:** `key` (bắt buộc) — copy từ field `path` trong response upload.

Ứng dụng trả **HTTP 302** và header `Location` trỏ tới **URL đã ký (presigned)**, hiệu lực **10 phút**. Trình duyệt sẽ hiển thị hoặc tải file tùy loại và `Content-Type`.

### Mở trực tiếp trên trình duyệt

```
http://localhost:8080/api/demo/minio/preview?key=<KEY>
```

Nếu `key` có ký tự đặc biệt, hãy **URL-encode** giá trị `key`.

### Ví dụ `curl` (xem redirect)

```bash
curl -s -D - -o /dev/null \
  "http://localhost:8080/api/demo/minio/preview?key=YOUR_KEY_HERE"
```

Bạn sẽ thấy `HTTP/1.1 302` và `Location: ...` (link MinIO có chữ ký tạm thời).

---

## 3) (Tuỳ chọn) Health — kiểm tra bucket và credential

- **URL:** `GET /api/demo/minio/health`

```bash
curl -s http://localhost:8080/api/demo/minio/health
```

---

## 4) Batch backfill document_versions (demo)

- **URL:** `POST /api/demo/minio/backfill-versions`
- **Mục đích:** xử lý dữ liệu cũ trong `documents`:
  - Nếu thiếu bản ghi version 1 trong `document_versions` -> tạo mới
  - Nếu `documents.current_version_id` đang `NULL` -> gắn vào version 1
- **Query (tuỳ chọn):**
  - `chunkSize` (mặc định `500`) - số record reader lấy mỗi đợt
  - `maxRounds` (mặc định `200`) - giới hạn số vòng chạy để tránh job quá dài

API này chạy bằng **Spring Batch Job** (`Job`/`Step`/`ItemReader`/`ItemWriter`), không phải service loop thủ công.
Spring Batch can bang he thong `BATCH_*` (metadata tables). Profile `local` da bat `spring.batch.jdbc.initialize-schema=always` de tu tao bang khi khoi dong.
Khi gặp lỗi dữ liệu đã cấu hình `skip`, bản ghi lỗi sẽ được lưu vào bảng `batch_failed_records`.
Step đang chạy theo chế độ multi-thread với `DEFAULT_WORKERS=4` để xử lý nhanh hơn.
Lỗi hạ tầng tạm thời (transient/lock) được retry tối đa 3 lần; nếu vẫn lỗi sẽ bị skip và lưu vào `batch_failed_records`.
Nếu cùng một record lỗi lặp lại do retry, hệ thống sẽ upsert theo khóa lỗi và tăng `retry_count` (không tạo quá nhiều dòng trùng).

### Ví dụ `curl`

```bash
curl -X POST \
  "http://localhost:8080/api/demo/minio/backfill-versions?chunkSize=200&maxRounds=100"
```

### Response (JSON)

```json
{
  "ok": true,
  "exitStatus": "COMPLETED",
  "status": "COMPLETED",
  "executionId": 42,
  "readCount": 1500,
  "writeCount": 1500,
  "chunkSize": 200,
  "maxRounds": 100
}
```

### Kiểm tra bản ghi bị lỗi (skip)

```sql
SELECT job_name, execution_id, step_name, phase, record_key, error_type, error_message, created_at
FROM batch_failed_records
WHERE job_name = 'documentVersionBackfillJob'
ORDER BY created_at DESC
LIMIT 100;
```

---

## Lưu ý bảo mật (chỉ dùng học local)

Các endpoint demo đang được mở `permitAll`. Trên môi trường thật cần: xác thực, whitelist loại file, giới hạn dung lượng, quét mã độc, và kiểm soát việc cấp presigned URL.
