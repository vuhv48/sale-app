package com.klb.app.persistence.postgres;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Một chỗ tập trung gọi function/procedure PostgreSQL từ Java.
 * <p>
 * Định nghĩa SQL nằm trong {@code db/migration/R__postgresql_functions.sql} (Flyway repeatable).
 */
@Repository
@RequiredArgsConstructor
public class PostgresRoutines {

	private final JdbcTemplate jdbcTemplate;

	// Ví dụ sau khi tạo fn_demo_add trong R__postgresql_functions.sql:
	// public int demoAdd(int a, int b) {
	// 	return jdbcTemplate.queryForObject("SELECT public.fn_demo_add(?, ?)", Integer.class, a, b);
	// }
}
