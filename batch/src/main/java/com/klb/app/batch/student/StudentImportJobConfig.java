package com.klb.app.batch.student;

import com.klb.app.persistence.entity.Student;
import com.klb.app.persistence.repository.StudentRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StudentImportJobConfig {

	public static final String JOB_NAME = "studentCsvImportJob";

	@Bean
	public FlatFileItemReader<StudentCsvLine> studentCsvReader() {
		// Spring Batch 6+: không còn constructor rỗng — cần LineMapper (và có thể setResource / setLinesToSkip sau).
		FlatFileItemReader<StudentCsvLine> reader = new FlatFileItemReader<>(studentLineMapper());
		reader.setResource(new ClassPathResource("batch/sample-students.csv"));
		reader.setLinesToSkip(1);
		return reader;
	}

	private LineMapper<StudentCsvLine> studentLineMapper() {
		DefaultLineMapper<StudentCsvLine> mapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("studentCode", "fullName");
		mapper.setLineTokenizer(tokenizer);
		FieldSetMapper<StudentCsvLine> fieldMapper = fs -> new StudentCsvLine(
				fs.readString("studentCode"),
				fs.readString("fullName")
		);
		mapper.setFieldSetMapper(fieldMapper);
		return mapper;
	}

	@Bean
	public RepositoryItemWriter<Student> studentItemWriter(StudentRepository studentRepository) {
		// Spring Batch 6+: repository bắt buộc truyền vào constructor.
		RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>(studentRepository);
		writer.setMethodName("save");
		return writer;
	}

	@Bean
	public Step studentImportStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			FlatFileItemReader<StudentCsvLine> studentCsvReader,
			StudentImportItemProcessor studentImportItemProcessor,
			RepositoryItemWriter<Student> studentItemWriter
	) {
		return new StepBuilder("studentImportStep", jobRepository)
				.<StudentCsvLine, Student>chunk(10, transactionManager)
				.reader(studentCsvReader)
				.processor(studentImportItemProcessor)
				.writer(studentItemWriter)
				.build();
	}

	@Bean(name = JOB_NAME)
	public Job studentCsvImportJob(JobRepository jobRepository, Step studentImportStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.start(studentImportStep)
				.build();
	}
}
