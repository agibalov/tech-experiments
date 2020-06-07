package io.agibalov;

import lombok.Builder;
import lombok.Cleanup;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
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
    public CommandLineRunner commandLineRunner(DataSource primaryDataSource) {
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

            TestDataGenerator testDataGenerator = new TestDataGenerator(20, 20, 20);
            long globalStartTime = System.currentTimeMillis();
            int totalRows = 0;
            for (Activity activity : Arrays.asList(schoolsActivity, classesActivity, studentsActivity)) {
                log.info("Populating {}", activity.getTableName());

                long startTime = System.currentTimeMillis();

                JdbcTemplateBatchUpdateTestDataWriter testDataWriter = new JdbcTemplateBatchUpdateTestDataWriter(
                        activity.getTableName(),
                        activity.getInsertSqlStatement(),
                        jdbcTemplate,
                        100); // TODO: use bigger batch size when running in AWS
                testDataGenerator.generate(testDataWriter);
                testDataWriter.flush();

                float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.f;
                float rowsPerSecond = testDataWriter.getRowCount() / elapsedTime;
                totalRows += testDataWriter.getRowCount();

                log.info("{}: {} rows in {} seconds ({} rows per second)",
                        activity.getTableName(),
                        testDataWriter.getRowCount(),
                        String.format("%.3f", elapsedTime),
                        String.format("%.0f", rowsPerSecond));
            }

            float globalElapsedTime = (System.currentTimeMillis() - globalStartTime) / 1000.f;
            log.info("Finished in {}. Total {} rows, {} rows per second",
                    String.format("%.3f", globalElapsedTime),
                    totalRows,
                    String.format("%.0f", totalRows / globalElapsedTime));
        };
    }

    @Builder
    @Value
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
