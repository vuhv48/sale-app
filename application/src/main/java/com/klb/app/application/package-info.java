/**
 * Application layer: use case orchestration, {@code @Service}, transaction boundaries.
 * <p>
 * Phụ thuộc {@code persistence} (repository/entity) nhưng không phụ thuộc {@code web} hay {@code batch}.
 * REST và batch chỉ gọi vào đây để logic nghiệp vụ tập trung một chỗ, dễ test và mở rộng.
 */
package com.klb.app.application;
