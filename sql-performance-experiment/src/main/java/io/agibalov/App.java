package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return new SingleConnectionDataSource();
    }

    @Bean
    public DataConfiguration dataConfiguration() {
        return DataConfiguration.builder()
                .numberOfAccounts(3)
                .numberOfUsersPerAccount(5)
                .build();
    }

    @Profile("mysql")
    @Bean
    public CommandLineRunner mysqlCommandLineRunner(
            NamedParameterJdbcTemplate jdbcTemplate,
            DataConfiguration dataConfiguration) {

        return new PopulateMysqlCommandLineRunner(
                jdbcTemplate,
                dataConfiguration);
    }

    @Profile("postgres")
    @Bean
    public CommandLineRunner postgresCommandLineRunner(
            NamedParameterJdbcTemplate jdbcTemplate,
            DataConfiguration dataConfiguration) {

        return new PopulatePostgresCommandLineRunner(
                jdbcTemplate,
                dataConfiguration);
    }
}
