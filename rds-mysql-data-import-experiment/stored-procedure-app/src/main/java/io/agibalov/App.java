package io.agibalov;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

@SpringBootApplication
@Slf4j
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            @Value("${app.schools}") int numberOfSchools,
            @Value("${app.classes}") int numberOfClasses,
            @Value("${app.students}") int numberOfStudents,
            @Value("${app.test-run-id}") String testRunId,
            NamedParameterJdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.update("delete from Students", Collections.emptyMap());
            jdbcTemplate.update("delete from Classes", Collections.emptyMap());
            jdbcTemplate.update("delete from Schools", Collections.emptyMap());

            long startTime = System.currentTimeMillis();
            jdbcTemplate.update(
                    "call GenerateData(:numberOfSchools, :numberOfClasses, :numberOfStudents)",
                    new MapSqlParameterSource()
                            .addValue("numberOfSchools", numberOfSchools)
                            .addValue("numberOfClasses", numberOfClasses)
                            .addValue("numberOfStudents", numberOfStudents));
            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.f;
            int totalRows = numberOfSchools +
                    numberOfSchools * numberOfClasses +
                    numberOfSchools * numberOfClasses * numberOfStudents;
            log.info("Finished in {}. Total {} rows, {} rows per second",
                    String.format("%.3f", elapsedTime),
                    totalRows,
                    String.format("%.0f", totalRows / elapsedTime));

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(TestResult.builder()
                    .testRunId(testRunId)
                    .approach("stored-procedure")
                    .numberOfSchools(numberOfSchools)
                    .numberOfClasses(numberOfClasses)
                    .numberOfStudents(numberOfStudents)
                    .numberOfRows(totalRows)
                    .time(elapsedTime)
                    .build()));
        };
    }
}
