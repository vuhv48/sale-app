package com.klb.app.application.batch;

/**
 * Metadata một lần chạy job — tương ứng các field thường lấy từ {@code JobExecution} / {@code JobInstance} (Spring Batch).
 * <ul>
 *     <li>{@code jobName} — tên job (định nghĩa trong config).</li>
 *     <li>{@code jobInstanceId} — id job instance (job + job parameters định danh một “phiên”).</li>
 *     <li>{@code jobExecutionId} — id một lần thực thi cụ thể (hoặc lần restart).</li>
 *     <li>{@code status} — batch status (COMPLETED, FAILED, …).</li>
 *     <li>{@code exitStatus} — mã kết thúc nghiệp vụ (exit code).</li>
 * </ul>
 */
public record DemoGreetingJobResult(
		String jobName,
		long jobInstanceId,
		long jobExecutionId,
		String status,
		String exitStatus
) {
}
