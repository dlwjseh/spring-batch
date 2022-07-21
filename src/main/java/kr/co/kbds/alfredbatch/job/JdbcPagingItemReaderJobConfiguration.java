package kr.co.kbds.alfredbatch.job;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import kr.co.kbds.alfredbatch.domain.bard.BardDailyLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcPagingItemReaderJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource bardDatasource;

	private final int chunkSize = 10; // Reader & Writer가 묶일 Chunk 트랜잭션 범위

	@Bean
	public Job jdbcPagingItemReaderJob() throws Exception {
		return jobBuilderFactory.get("jdbcPagingItemReaderJob")
				.start(jdbcPagingItemReaderStep())
				.build();
	}

	@Bean
	public Step jdbcPagingItemReaderStep() throws Exception {
		return stepBuilderFactory.get("jdbcPagingItemReaderStep")
				.<BardDailyLogin, BardDailyLogin>chunk(chunkSize)
				.reader(jdbcPagingItemReader())
				.writer(jdbcPagingItemWriter())
				.build();
	}

	@Bean
	public JdbcPagingItemReader<BardDailyLogin> jdbcPagingItemReader() throws Exception {
		return new JdbcPagingItemReaderBuilder<BardDailyLogin>()
				.pageSize(chunkSize)
				.fetchSize(chunkSize)
				.dataSource(bardDatasource)
				.rowMapper(new BeanPropertyRowMapper<>(BardDailyLogin.class))
				.queryProvider(createQueryProvider())
				.name("jdbcPagingItemReader")
				.build();
	}

	@Bean
	public ItemWriter<BardDailyLogin> jdbcPagingItemWriter() {
		return list -> {
			for (BardDailyLogin bardDailyLogin : list) {
				log.info("DailyLogin : {}", bardDailyLogin);
			}
		};
	}

//	@Bean
	public PagingQueryProvider createQueryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
		queryProvider.setDataSource(bardDatasource);
		queryProvider.setSelectClause("login_year_month, login_day, product_id, login_count");
		queryProvider.setFromClause("from daily_login");

		/*
		  멀티 키를 가진 Entity일 경우에 멀티키 모두를 해줘야 함.
		  하나만 했더니 1페이지만 불러오거나 일부만 불러오는 등의 문제가 있었음

		  정확히는 같은값을 가진 로우 갯수가 pageSize보다 클경우 가져오고 나서 다음에 가져올 때
		  sortkey를 where절에 가져왔던 것보다 큰것을 조회해서 못 가져오는 현상
		 */
		Map<String, Order> sortKeys = new HashMap<>(3);
		sortKeys.put("login_year_month", Order.ASCENDING);
		sortKeys.put("login_day", Order.ASCENDING);
		sortKeys.put("product_id", Order.ASCENDING);
		queryProvider.setSortKeys(sortKeys);

		return queryProvider.getObject();
	}
}
