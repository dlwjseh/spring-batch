package kr.co.kbds.alfredbatch.job;

import kr.co.kbds.alfredbatch.domain.MonthlyLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcBatchItemWriterJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource bardDatasource;
    @Qualifier("alfredDataSource") private final DataSource alfredDataSource;

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcBatchItemWriterJob() throws Exception {
        return jobBuilderFactory.get("jdbcBatchItemWriterJob")
                .start(jdbcBatchItemWriterStep())
                .build();
    }

    @Bean
    public Step jdbcBatchItemWriterStep() throws Exception {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
                .<MonthlyLogin, MonthlyLogin>chunk(chunkSize)
                .reader(jdbcBatchItemWriterReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<MonthlyLogin> jdbcBatchItemWriterReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<MonthlyLogin>()
                .dataSource(bardDatasource)
                .fetchSize(chunkSize)
                .pageSize(chunkSize)
                .rowMapper(new BeanPropertyRowMapper<>(MonthlyLogin.class))
                .queryProvider(createQueryProvider())
                .name("jdbcBatchItemWriter")
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<MonthlyLogin> jdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<MonthlyLogin>()
                .dataSource(alfredDataSource)
                .sql("INSERT INTO monthly_login (login_year_month, product_id, login_count) " +
                        "VALUES (:loginYearMonth, :productId, :loginCount)")
                .beanMapped()
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(bardDatasource);
        queryProvider.setSelectClause("login_year_month, product_id, SUM(login_count) as login_count");
        queryProvider.setFromClause("from daily_login");
        queryProvider.setGroupClause("login_year_month, product_id");

        Map<String, Order> sortKeys = new HashMap<>(3);
        sortKeys.put("login_year_month", Order.ASCENDING);
        sortKeys.put("product_id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }
}
