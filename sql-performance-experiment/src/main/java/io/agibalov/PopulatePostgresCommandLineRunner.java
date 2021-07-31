package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
public class PopulatePostgresCommandLineRunner implements CommandLineRunner {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataConfiguration dataConfiguration;

    public PopulatePostgresCommandLineRunner(
            NamedParameterJdbcTemplate jdbcTemplate,
            DataConfiguration dataConfiguration) {

        this.jdbcTemplate = jdbcTemplate;
        this.dataConfiguration = dataConfiguration;
    }

    @Override
    public void run(String... args) {
        log.info("todo");
    }
}
