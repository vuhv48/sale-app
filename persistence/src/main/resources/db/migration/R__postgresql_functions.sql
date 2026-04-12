-- =============================================================================
-- PostgreSQL functions / procedures gọi từ Java (JdbcTemplate trong PostgresRoutines).
-- File repeatable (R__): Flyway chạy lại khi nội dung đổi — hợp với CREATE OR REPLACE.
-- =============================================================================

SELECT 1 WHERE false;

-- Ví dụ (xóa hoặc thay bằng function thật của bạn):
-- CREATE OR REPLACE FUNCTION public.fn_demo_add(a integer, b integer)
-- RETURNS integer
-- LANGUAGE sql
-- IMMUTABLE
-- AS $$
--     SELECT a + b
-- $$;
