package kr.co.kbds.alfredbatch.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties
public class DataSourceConfig {

	@Primary
	@Bean("bardDatasource")
	@ConfigurationProperties(prefix = "spring.datasource.bard")
	public DataSource bardDatasource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean("alfredDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.alfred")
	public DataSource alfredDataSource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

}
