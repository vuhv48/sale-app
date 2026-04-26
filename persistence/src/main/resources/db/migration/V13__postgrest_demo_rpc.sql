-- =============================================================================
-- PostgREST demo (local): SELECT customers -> POST /rpc/list_customers
-- docker compose up -d postgrest  ->  http://localhost:3001
--
-- SECURITY DEFINER: postgrest_anon khong co app.* nen RLS se chan neu khong dung cach nay.
-- Chi dev — production can JWT + role + policy ro rang.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS demo_rpc;

-- Neu tung thu V13 ban dau (add_ints/greeting)
DROP FUNCTION IF EXISTS demo_rpc.add_ints(integer, integer);
DROP FUNCTION IF EXISTS demo_rpc.greeting(text);

CREATE OR REPLACE FUNCTION demo_rpc.list_customers(
    p_limit integer DEFAULT 50,
    p_offset integer DEFAULT 0
)
RETURNS TABLE (
    id uuid,
    customer_code varchar(32),
    name varchar(255),
    phone varchar(32),
    email varchar(255),
    is_active boolean,
    created_at timestamptz
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT
        c.id,
        c.customer_code,
        c.name,
        c.phone,
        c.email,
        c.is_active,
        c.created_at
    FROM customers c
    WHERE c.is_deleted = FALSE
    ORDER BY c.customer_code ASC
    LIMIT LEAST(GREATEST(coalesce(p_limit, 50), 1), 500)
    OFFSET GREATEST(coalesce(p_offset, 0), 0);
$$;

CREATE ROLE postgrest_anon NOLOGIN;
CREATE ROLE postgrest_authenticator LOGIN PASSWORD 'postgrest_dev_secret' NOINHERIT;

GRANT postgrest_anon TO postgrest_authenticator;

GRANT CONNECT ON DATABASE sale_app TO postgrest_authenticator;
GRANT USAGE ON SCHEMA demo_rpc TO postgrest_anon;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA demo_rpc TO postgrest_anon;
ALTER DEFAULT PRIVILEGES IN SCHEMA demo_rpc GRANT EXECUTE ON FUNCTIONS TO postgrest_anon;
