package com.klb.app.batch.demo;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job học tay: CSV → Processor nối chuỗi → Writer in log.
 * <ul>
 *     <li>{@code RETRY_DEMO} — {@code retry} + {@link DemoGreetingTransientException} (lần 1 lỗi tạm thời, lần 2 OK).</li>
 *     <li>{@code -} — {@code skip} + {@link DemoGreetingSkipListener}.</li>
 * </ul>
 */
@Configuration
public class DemoGreetingJobConfig {

	public static final String JOB_NAME = "demoGreetingJob";

	@Bean
	public FlatFileItemReader<DemoGreetingLine> demoGreetingReader() {
		FlatFileItemReader<DemoGreetingLine> reader = new FlatFileItemReader<>(demoGreetingLineMapper());
		reader.setResource(new ClassPathResource("batch/demo-greeting.csv"));
		reader.setLinesToSkip(1);
		return reader;
	}

	private LineMapper<DemoGreetingLine> demoGreetingLineMapper() {
		DefaultLineMapper<DemoGreetingLine> mapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("name");
		mapper.setLineTokenizer(tokenizer);
		FieldSetMapper<DemoGreetingLine> fieldMapper = fs -> new DemoGreetingLine(fs.readString("name").trim());
		mapper.setFieldSetMapper(fieldMapper);
		return mapper;
	}

	/**
	 * {@link StepScope}: mỗi lần chạy step một instance mới — state {@code retryDemoTried} reset đúng cho demo retry.
	 */
	@Bean
	@StepScope
	public ItemProcessor<DemoGreetingLine, String> demoGreetingProcessor() {
		return new ItemProcessor<>() {
			private boolean retryDemoTried;

			@Override
			public String process(DemoGreetingLine line) {
				if (line.name().isBlank()) {
					throw new IllegalArgumentException("Ten trong");
				}
				if ("-".equals(line.name())) {
					throw new IllegalArgumentException("Ban ghi danh dau bo qua (demo skip)");
				}
				if ("RETRY_DEMO".equals(line.name()) && !retryDemoTried) {
					retryDemoTried = true;
					throw new DemoGreetingTransientException("Loi tam thoi — framework se goi lai process cung ban ghi");
				}
				return "Xin chao, " + line.name() + "!";
			}
		};
	}

	@Bean
	public ItemWriter<String> demoGreetingWriter() {
		return chunk -> {
			for (String msg : chunk.getItems()) {
				System.out.println("[demo-greeting] " + msg);
			}
		};
	}

	@Bean
	public Step demoGreetingStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			FlatFileItemReader<DemoGreetingLine> demoGreetingReader,
			ItemProcessor<DemoGreetingLine, String> demoGreetingProcessor,
			ItemWriter<String> demoGreetingWriter,
			DemoGreetingSkipListener demoGreetingSkipListener
	) {
		return new StepBuilder("demoGreetingStep", jobRepository)
				.<DemoGreetingLine, String>chunk(2, transactionManager)
				.reader(demoGreetingReader)
				.processor(demoGreetingProcessor)
				.writer(demoGreetingWriter)
				.faultTolerant()
				.retry(DemoGreetingTransientException.class)
				.retryLimit(3)
				.skip(IllegalArgumentException.class)
				.skipLimit(50)
				.listener(demoGreetingSkipListener)
				.build();
	}

	@Bean(name = JOB_NAME)
	public Job demoGreetingJob(JobRepository jobRepository, Step demoGreetingStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.start(demoGreetingStep)
				.build();
	}
}
