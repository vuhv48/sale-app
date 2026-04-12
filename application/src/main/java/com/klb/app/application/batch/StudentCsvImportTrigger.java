package com.klb.app.application.batch;

/**
 * Cổng kích hoạt job import (triển khai trong module {@code batch}).
 */
public interface StudentCsvImportTrigger {

	StudentCsvImportResult run();
}
