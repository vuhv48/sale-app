-- =============================================================================
-- FILE: V11__customers_rls.sql
-- PURPOSE (EN):
-- - Create dedicated DB login for application runtime (non-superuser, no bypassrls).
-- - Enable PostgreSQL RLS for customers with data_scope support.
-- - Expose stable read functions get_customers/get_customers_count for application layer.
-- MUC DICH:
-- - Tao tai khoan DB runtime rieng cho app (khong superuser, khong bypassrls).
-- - Bat RLS cho customers co ho tro data_scope (ALL/OWN/NONE...).
-- - Cung cap function on dinh cho tang ung dung goi du lieu customer.
-- =============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sale_app_user') THEN
        CREATE ROLE sale_app_user LOGIN PASSWORD '';
    END IF;
END $$;

ALTER ROLE sale_app_user
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION
    NOBYPASSRLS;

GRANT CONNECT ON DATABASE sale_app TO sale_app_user;
GRANT USAGE ON SCHEMA public TO sale_app_user;
GRANT USAGE ON SCHEMA authz TO sale_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO sale_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA authz TO sale_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sale_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA authz TO sale_app_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO sale_app_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA authz TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA authz GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA authz GRANT USAGE, SELECT ON SEQUENCES TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO sale_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA authz GRANT EXECUTE ON FUNCTIONS TO sale_app_user;

CREATE OR REPLACE FUNCTION set_rls_context(
    p_user_role TEXT,
    p_username TEXT,
    p_data_scope TEXT
)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
DECLARE
    v_role TEXT;
    v_username TEXT;
    v_scope TEXT;
BEGIN
    v_role := upper(coalesce(p_user_role, 'USER'));
    v_username := coalesce(p_username, '');
    v_scope := upper(coalesce(p_data_scope, 'NONE'));
    PERFORM set_config('app.user_role', v_role, true);
    PERFORM set_config('app.username', v_username, true);
    PERFORM set_config('app.data_scope', v_scope, true);
    RETURN v_role;
END;
$$;

ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS customers_admin_all ON customers;
DROP POLICY IF EXISTS customers_block_all ON customers;
DROP POLICY IF EXISTS customers_scope_policy ON customers;
CREATE POLICY customers_scope_policy
    ON customers
    FOR ALL
    USING (
        upper(coalesce(current_setting('app.user_role', true), '')) = 'ADMIN'
        OR upper(coalesce(current_setting('app.data_scope', true), '')) = 'ALL'
        OR (
            upper(coalesce(current_setting('app.data_scope', true), '')) = 'OWN'
            AND coalesce(created_by, '') = coalesce(current_setting('app.username', true), '')
        )
    )
    WITH CHECK (
        upper(coalesce(current_setting('app.user_role', true), '')) = 'ADMIN'
        OR upper(coalesce(current_setting('app.data_scope', true), '')) = 'ALL'
        OR (
            upper(coalesce(current_setting('app.data_scope', true), '')) = 'OWN'
            AND coalesce(created_by, '') = coalesce(current_setting('app.username', true), '')
        )
    );

-- Expose customer read via function so application can call a stable DB API.
CREATE OR REPLACE FUNCTION get_customers(p_limit INT DEFAULT 20, p_offset INT DEFAULT 0)
RETURNS TABLE (
    id UUID,
    customer_code VARCHAR(32),
    name VARCHAR(255),
    phone VARCHAR(32),
    email VARCHAR(255),
    tax_code VARCHAR(64),
    address_line VARCHAR(500),
    is_active BOOLEAN,
    created_at TIMESTAMPTZ
) AS $$
    SELECT
        c.id,
        c.customer_code,
        c.name,
        c.phone,
        c.email,
        c.tax_code,
        c.address_line,
        c.is_active,
        c.created_at
    FROM customers c
    WHERE c.is_deleted = FALSE
    ORDER BY c.customer_code ASC
    LIMIT GREATEST(coalesce(p_limit, 20), 1)
    OFFSET GREATEST(coalesce(p_offset, 0), 0);
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION get_customers_count()
RETURNS BIGINT AS $$
    SELECT count(1)::BIGINT
    FROM customers c
    WHERE c.is_deleted = FALSE;
$$ LANGUAGE sql STABLE;
