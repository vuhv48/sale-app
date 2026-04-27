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

### Ví dụ `curl`

```bash
curl -s -F "file=@/đường/dẫn/ảnh.png" \
  http://localhost:8080/api/demo/minio/upload
```

### Response (JSON)

Ứng dụng trả JSON gồm tối thiểu:

- `bucket` — tên bucket trên MinIO
- `key` — mã object (dùng cho bước preview)
- `size` — kích thước file (bytes)
- `contentType` — MIME type (có thể rỗng nếu client không gửi)

Ví dụ:

```json
{
  "bucket": "sale-app-files",
  "key": "1745820000000-a1b2c3d4-...-ảnh.png",
  "size": 123456,
  "contentType": "image/png"
}
```

---

## 2) Preview (mở trong trình duyệt)

- **URL:** `GET /api/demo/minio/preview?key=<KEY>`
- **Tham số:** `key` (bắt buộc) — copy từ field `key` trong response upload.

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

## Lưu ý bảo mật (chỉ dùng học local)

Các endpoint demo đang được mở `permitAll`. Trên môi trường thật cần: xác thực, whitelist loại file, giới hạn dung lượng, quét mã độc, và kiểm soát việc cấp presigned URL.
