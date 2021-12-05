package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
public class PopulateMysqlCommandLineRunner implements CommandLineRunner {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataConfiguration dataConfiguration;

    public PopulateMysqlCommandLineRunner(
            NamedParameterJdbcTemplate jdbcTemplate,
            DataConfiguration dataConfiguration) {

        this.jdbcTemplate = jdbcTemplate;
        this.dataConfiguration = dataConfiguration;
    }

    @Override
    public void run(String... args) {
        log.info("MakeTemplateData");
        jdbcTemplate.update(
                "call MakeTemplateData(:users)",
                new MapSqlParameterSource()
                        .addValue("users", dataConfiguration.getNumberOfUsersPerAccount()));

        for (int i = 0; i < dataConfiguration.getNumberOfAccounts(); ++i) {
            log.info("MakeAccount {} of {}", i + 1, dataConfiguration.getNumberOfAccounts());
            jdbcTemplate.update(
                    "call MakeAccount()",
                    new MapSqlParameterSource());
        }

        log.info("DeleteTemplateData");
        jdbcTemplate.update(
                "call DeleteTemplateData()",
                new MapSqlParameterSource());
    }
}
