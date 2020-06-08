package io.agibalov;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
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
            DataSource primaryDataSource) {
        return args -> {
            @Cleanup Connection connection = primaryDataSource.getConnection();
            connection.setAutoCommit(false);
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, true);

            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

            for (Activity activity : Arrays.asList(studentsActivity, classesActivity, schoolsActivity)) {
                log.info("Deleting {}", activity.getTableName());

                String deleteSqlStatement = activity.getDeleteSqlStatement();
                jdbcTemplate.update(deleteSqlStatement, Collections.emptyMap());
            }

            TestDataGenerator testDataGenerator = new TestDataGenerator(
                    numberOfSchools, numberOfClasses, numberOfStudents);
            long startTime = System.currentTimeMillis();
            int totalRows = 0;
            for (Activity activity : Arrays.asList(schoolsActivity, classesActivity, studentsActivity)) {
                log.info("Populating {}", activity.getTableName());

                long activityStartTime = System.currentTimeMillis();

                JdbcTemplateBatchUpdateTestDataWriter testDataWriter = new JdbcTemplateBatchUpdateTestDataWriter(
                        activity.getTableName(),
                        activity.getInsertSqlStatement(),
                        jdbcTemplate,
                        1000);
                testDataGenerator.generate(testDataWriter);
                testDataWriter.flush();

                float activityElapsedTime = (System.currentTimeMillis() - activityStartTime) / 1000.f;
                float rowsPerSecond = testDataWriter.getRowCount() / activityElapsedTime;
                totalRows += testDataWriter.getRowCount();

                log.info("{}: {} rows in {} seconds ({} rows per second)",
                        activity.getTableName(),
                        testDataWriter.getRowCount(),
                        String.format("%.3f", activityElapsedTime),
                        String.format("%.0f", rowsPerSecond));
            }

            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.f;
            log.info("Finished in {}. Total {} rows, {} rows per second",
                    String.format("%.3f", elapsedTime),
                    totalRows,
                    String.format("%.0f", totalRows / elapsedTime));

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(TestResult.builder()
                    .testRunId(testRunId)
                    .approach("jdbc-batch-insert")
                    .numberOfSchools(numberOfSchools)
                    .numberOfClasses(numberOfClasses)
                    .numberOfStudents(numberOfStudents)
                    .numberOfRows(totalRows)
                    .time(elapsedTime)
                    .build()));
        };
    }

    @Builder
    @Data
    private static class Activity {
        private TableName tableName;
        private String deleteSqlStatement;
        private String insertSqlStatement;
    }

    private static final Activity schoolsActivity = Activity.builder()
            .tableName(TableName.Schools)
            .deleteSqlStatement("delete from Schools")
            .insertSqlStatement("insert into Schools(id, name) values(:id, :name)")
            .build();
    private static final Activity classesActivity = Activity.builder()
            .tableName(TableName.Classes)
            .deleteSqlStatement("delete from Classes")
            .insertSqlStatement("insert into Classes(id, schoolId, name) values(:id, :schoolId, :name)")
            .build();
    private static final Activity studentsActivity = Activity.builder()
            .tableName(TableName.Students)
            .deleteSqlStatement("delete from Students")
            .insertSqlStatement("insert into Students(id, classId, name) values(:id, :classId, :name)")
            .build();
}
