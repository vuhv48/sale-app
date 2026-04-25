-- Cap nhat dong Flyway trong DB sau khi ban SUA lai file V2__*.sql ma migration do da chay truoc do.
-- Neu khong sua V2 thi khong can file nay — uu tien dung: ./mvnw -pl bootstrap flyway:repair ...
--
-- Gia tri checksum phai TRUNG voi log loi Spring Boot dong "Resolved locally".
-- Vi du cu: Resolved locally : 1273483458

UPDATE flyway_schema_history
SET checksum = 1273483458
WHERE version = '2';

-- Neu khong cap nhat duoc, lay checksum moi tu log moi nhat roi chay lai UPDATE.
