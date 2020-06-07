package io.agibalov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@SpringBootApplication
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(NamedParameterJdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("Hello world");

            jdbcTemplate.update("delete from Schools", new MapSqlParameterSource());

            jdbcTemplate.update(
                    "insert into Schools(id, name) values (:id, :name)",
                    new MapSqlParameterSource()
                            .addValue("id", "1")
                            .addValue("name", "School 1"));
        };
    }
}
