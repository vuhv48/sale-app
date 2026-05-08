package com.klb.app.application.service.impl.sales.sync;

import com.klb.app.application.service.sales.sync.CustomerSyncEvent;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerSyncUpsertService {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	/**
	 * Bulk upsert (PostgreSQL): insert theo customer_code, neu trung thi update (idempotent theo unique key).
	 * De "2tr record" chay nhanh, bat buoc bulk/batch thay vi repository save tung ban ghi.
	 */
	@Transactional
	public void bulkUpsert(List<CustomerSyncEvent> events) {
		if (events == null || events.isEmpty()) {
			return;
		}
		// customers table dang bat RLS; can set context cho transaction hien tai truoc khi upsert.
		jdbcTemplate.getJdbcTemplate().execute("select set_rls_context('ADMIN','kafka-sync','ALL')");

		String sql = """
				INSERT INTO customers(
					customer_code, name, phone, email, tax_code, address_line, is_active,
					is_deleted, created_at, updated_at
				) VALUES (
					:customerCode, :name, :phone, :email, :taxCode, :addressLine, :active,
					false, :now, :now
				)
				ON CONFLICT (customer_code) DO UPDATE SET
					name = EXCLUDED.name,
					phone = EXCLUDED.phone,
					email = EXCLUDED.email,
					tax_code = EXCLUDED.tax_code,
					address_line = EXCLUDED.address_line,
					is_active = EXCLUDED.is_active,
					is_deleted = false,
					updated_at = EXCLUDED.updated_at
				""";

		Instant now = Instant.now();
		Timestamp nowTs = Timestamp.from(now);
		MapSqlParameterSource[] batch = events.stream()
				.map(e -> new MapSqlParameterSource()
						.addValue("customerCode", requiredTrim(e.customerCode(), "customerCode"))
						.addValue("name", requiredTrim(e.name(), "name"))
						.addValue("phone", optionalTrim(e.phone()))
						.addValue("email", optionalTrim(e.email()))
						.addValue("taxCode", optionalTrim(e.taxCode()))
						.addValue("addressLine", optionalTrim(e.addressLine()))
						.addValue("active", e.active() != null ? e.active() : Boolean.TRUE)
						.addValue("now", nowTs))
				.toArray(MapSqlParameterSource[]::new);

		jdbcTemplate.batchUpdate(sql, batch);
	}

	private static String requiredTrim(String value, String field) {
		if (!StringUtils.hasText(value)) {
			throw new DomainException(ErrorStatus.VALIDATION_ERROR, "Field is required: " + field);
		}
		return value.trim();
	}

	private static String optionalTrim(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}

