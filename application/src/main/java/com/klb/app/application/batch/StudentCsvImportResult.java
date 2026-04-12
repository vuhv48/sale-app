package com.klb.app.application.batch;

/**
 * Kết quả chạy job import CSV sinh viên — tách khỏi Spring Batch để {@code web} không phụ thuộc {@code batch}.
 */
public record StudentCsvImportResult(String exitStatus, String status, long executionId) {
}
