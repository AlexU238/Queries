package oleksii.queriestask.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {

    @Bean(name = "clickhouseDataSource")
    @ConfigurationProperties(prefix = "app.datasource.clickhouse")
    public DataSource clickhouseDataSource() {
        // Explicitly using Hikari for ClickHouse connection pooling
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "clickhouseJdbcTemplate")
    public NamedParameterJdbcTemplate clickhouseJdbcTemplate(
            @Qualifier("clickhouseDataSource") DataSource dataSource) {
        // This is the direct query tool that executes custom analytical strings
        return new NamedParameterJdbcTemplate(dataSource);
    }
}