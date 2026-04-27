# MinIO upload (local dev)

Tài liệu API (upload + preview): `docs/minio-api.md`.

## 1) Start MinIO

```bash
docker compose up -d minio
```

- Console: `http://localhost:9005` (user/pass dev: `minioadmin` / `minioadmin`)
- S3 API: `http://localhost:9000`

Create bucket: `sale-app-files` (must match `app.storage.minio.bucket`).

Hoặc để app tự tạo bucket ở local: `app.storage.minio.auto-create-bucket=true` (profile `local` đã bật mặc định).

## 2) Run Spring Boot with profile `local`

Profile `local` enables MinIO integration in `bootstrap/src/main/resources/application-local.yaml`.

## 3) Health check (bucket exists + credentials OK)

```bash
curl -s http://localhost:8080/api/demo/minio/health
```

## 4) Upload a file

```bash
curl -s -F "file=@./README.md" http://localhost:8080/api/demo/minio/upload
```

Response contains `bucket` + `key`. Open MinIO console and browse the object.

## 5) Preview in browser (presigned redirect)

After upload, copy `key` from JSON, then open in browser (URL-encode if needed):

`http://localhost:8080/api/demo/minio/preview?key=<KEY_HERE>`

Or:

```bash
curl -s -D - -o /dev/null "http://localhost:8080/api/demo/minio/preview?key=YOUR_KEY"
```

You should see a `302` `Location:` pointing at MinIO with a short-lived signature.

## Notes

- This demo endpoint is `permitAll` for local learning. In production, protect uploads with auth + virus scan + size limits + content-type allowlist.
- Moving to real AWS S3 later is mostly changing endpoint/credentials + turning off path-style if not needed.
