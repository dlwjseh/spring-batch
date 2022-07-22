package kr.co.kbds.alfredbatch.job;

import kr.co.kbds.alfredbatch.TestBatchConfig;
import kr.co.kbds.alfredbatch.config.DataSourceConfig;
import kr.co.kbds.alfredbatch.domain.DailyLogin;
import kr.co.kbds.alfredbatch.domain.bard.BardDailyLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {TestBatchConfig.class, ProcessorConvertJobConfiguration.class, DataSourceConfig.class})
class ProcessorConvertJobConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private DataSource bardDataSource;
    @Autowired
    @Qualifier("alfredDataSource")
    private DataSource alfredDataSource;

    private JdbcTemplate bardJdbcTemplate;
    private JdbcTemplate alfredJdbcTemplate;

    @BeforeEach
    void before() {
        bardJdbcTemplate = new JdbcTemplate(bardDataSource);
        alfredJdbcTemplate = new JdbcTemplate(alfredDataSource);
    }

    @Test
    void test() throws Exception {
        // given
        String yearMonth = "202207";
        List<BardDailyLogin> list = new ArrayList<>();
        for (int i=1; i<=30; i++) {
            String day = (i < 10 ? "0" : "") + i;
            list.add(new BardDailyLogin(yearMonth, day, 1L, i * 3));
        }

        StringBuilder builder = new StringBuilder("INSERT INTO daily_login (login_year_month, login_day, product_id, login_count) VALUES ");
        list.forEach(l -> builder.append(String.format("(%s, %s, %d, %d),", l.getLoginYearMonth(), l.getLoginDay(), l.getProductId(), l.getLoginCount())));
        String sql = builder.deleteCharAt(builder.length() - 1).toString();
        bardJdbcTemplate.execute(sql);

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        String selectQuery = "SELECT login_day, product_id, login_count FROM daily_login ORDER BY login_day";
        List<DailyLogin> dailyLogins = alfredJdbcTemplate.query(selectQuery,
                new BeanPropertyRowMapper<>(DailyLogin.class));
        BardDailyLogin bard;
        DailyLogin alfred;
        for (int i=0; i<30; i++) {
            bard = list.get(i);
            alfred = dailyLogins.get(i);

            String loginDay = bard.getLoginYearMonth() + (bard.getLoginDay().length() > 1 ? "" : "0") + bard.getLoginDay();
            assertThat(alfred.getLoginDay()).isEqualTo(loginDay);
            assertThat(alfred.getProductId()).isEqualTo(bard.getProductId());
            assertThat(alfred.getLoginCount()).isEqualTo(bard.getLoginCount());
        }

        // reset
        bardJdbcTemplate.execute("DELETE FROM daily_login");
        alfredJdbcTemplate.execute("DELETE FROM daily_login");
    }

}