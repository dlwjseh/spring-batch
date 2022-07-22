package kr.co.kbds.alfredbatch.job;

import kr.co.kbds.alfredbatch.domain.DailyLogin;
import kr.co.kbds.alfredbatch.domain.bard.BardDailyLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class ProcessorConvertJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource bardDatasource;
    @Qualifier("alfredDataSource") private final DataSource alfredDatasource;

    public static final String JOB_NAME = "ProcessorConvertBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";
    private static final int chunkSize = 10;

    @Bean
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .preventRestart()
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
                .<BardDailyLogin, DailyLogin>chunk(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<BardDailyLogin> reader() {
        return new JdbcCursorItemReaderBuilder<BardDailyLogin>()
                .dataSource(bardDatasource)
                .fetchSize(chunkSize)
                .rowMapper(new BeanPropertyRowMapper<>(BardDailyLogin.class))
                .sql("SELECT login_year_month, login_day, product_id, login_count FROM daily_login " +
                        "ORDER BY product_id, login_year_month, login_day")
                .name(BEAN_PREFIX + "reader")
                .build();
    }

    @Bean
    public ItemProcessor<BardDailyLogin, DailyLogin> processor() {
        return bardDaily -> {
            String loginDay = bardDaily.getLoginDay();
            return new DailyLogin(bardDaily.getLoginYearMonth()+ (loginDay.length()>1 ? "" : "0") + loginDay,
                    bardDaily.getProductId(), bardDaily.getLoginCount());
        };
    }

    @Bean
    public JdbcBatchItemWriter<DailyLogin> writer() {
        return new JdbcBatchItemWriterBuilder<DailyLogin>()
                .dataSource(alfredDatasource)
                .sql("INSERT INTO daily_login (login_day, product_id, login_count) " +
                        "VALUES (:loginDay, :productId, :loginCount)")
                .beanMapped()
                .build();
    }
}
