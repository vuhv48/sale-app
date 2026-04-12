/**
 * Application layer: use case orchestration, transaction boundaries.
 * <ul>
 *   <li>{@code com.klb.app.application.service…} — interface + DTO (theo subdomain {@code .auth}, {@code .student}); import bulk tách {@code StudentImportService}.</li>
 *   <li>{@code com.klb.app.application.service.impl…} — {@code @Service} ({@code *Impl}), triển khai interface tương ứng.</li>
 * </ul>
 * Phụ thuộc {@code persistence} (repository/entity) nhưng không phụ thuộc {@code web} hay {@code batch}.
 * REST và batch chỉ inject interface từ {@code service}, không tham chiếu trực tiếp {@code service.impl}.
 */
package com.klb.app.application;
