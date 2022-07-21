package kr.co.kbds.alfredbatch.job;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import kr.co.kbds.alfredbatch.domain.bard.BardDailyLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource bardDatasource;

	private final int chunkSize = 10; // Reader & Writer가 묶일 Chunk 트랜잭션 범위

	@Bean
	public Job jdbcCursorItemReaderJob() {
		return jobBuilderFactory.get("jdbcCursorItemReaderJob")
				.start(jdbcCursorItemReaderStep())
				.build();
	}

	@Bean
	public Step jdbcCursorItemReaderStep() {
		return stepBuilderFactory.get("jdbcCursorItemReaderStep")
				.<BardDailyLogin, BardDailyLogin>chunk(chunkSize)
				.reader(jdbcCursorItemReader())
				.writer(jdbcCursorItemWriter())
				.build();
	}

	@Bean
	public JdbcCursorItemReader<BardDailyLogin> jdbcCursorItemReader() {
		return new JdbcCursorItemReaderBuilder<BardDailyLogin>()
				.fetchSize(chunkSize) // Database에서 한번에 가져올 데이터 양
				.dataSource(bardDatasource)
				.rowMapper(new BeanPropertyRowMapper<>(BardDailyLogin.class)) // 쿼리 결과를 Java 인스턴스로 매핑하기 위한 Mapper
				.sql("SELECT login_year_month, login_day, product_id, login_count FROM daily_login")
				.name("jdbcCursorItemReader") // Bean이름 X. Spring Batch의 ExecutionContext에서 저장되어질 이름
				.build();
	}

	@Bean
	public ItemWriter<BardDailyLogin> jdbcCursorItemWriter() {
		return list -> {
			for (BardDailyLogin bardDailyLogin : list) {
				log.info("DailyLogin : {}", bardDailyLogin);
			}
		};
	}
}
