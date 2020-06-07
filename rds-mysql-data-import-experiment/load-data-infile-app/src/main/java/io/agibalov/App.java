package io.agibalov;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(NamedParameterJdbcTemplate jdbcTemplate) {
        return args -> {
            for (Activity activity : Arrays.asList(studentsActivity, classesActivity, schoolsActivity)) {
                String deleteSqlStatement = activity.getDeleteSqlStatement();
                jdbcTemplate.update(deleteSqlStatement, Collections.emptyMap());
            }

            final int numberOfSchools = 50;
            final int numberOfClasses = 50;
            final int numberOfStudents = 50;

            long startTime = System.currentTimeMillis();

            TestDataGenerator testDataGenerator = new TestDataGenerator(numberOfSchools, numberOfClasses, numberOfStudents);

            for (Activity activity : Arrays.asList(schoolsActivity, classesActivity, studentsActivity)) {
                CsvMapper csvMapper = new CsvMapper();
                CsvSchema schema = csvMapper.schemaFor(activity.getCsvSchemaClass())
                        .withColumnReordering(true)
                        .withNullValue("")
                        .withHeader();
                ObjectWriter objectWriter = csvMapper.writer(schema);
                File csvFile = new File(activity.getCsvFileName());
                try (SequenceWriter sequenceWriter = objectWriter.writeValues(csvFile)) {
                    CsvFileTestDataWriter csvFileTestDataWriter = new CsvFileTestDataWriter(
                            activity.getTableName(), sequenceWriter);
                    testDataGenerator.generate(csvFileTestDataWriter);
                }

                jdbcTemplate.update("load data local infile :csvFileName\n" +
                                "    into table Schools\n" +
                                "    fields terminated by ',' optionally enclosed by '\"' escaped by '\\\\'\n" +
                                "    lines terminated by '\\n' starting by ''\n" +
                                "    ignore 1 rows" +
                                ";",
                        new MapSqlParameterSource()
                                .addValue("csvFileName", csvFile.getAbsolutePath()));
            }

            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.f;
            int totalRows = numberOfSchools +
                    numberOfSchools * numberOfClasses +
                    numberOfSchools * numberOfClasses * numberOfStudents;
            log.info("Finished in {}. Total {} rows, {} rows per second",
                    String.format("%.3f", elapsedTime),
                    totalRows,
                    String.format("%.0f", totalRows / elapsedTime));
        };
    }

    @Data
    @Builder
    @JsonPropertyOrder({"id", "name"})
    public static class School {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @JsonPropertyOrder({"id", "schoolId", "name"})
    public static class Klass {
        private String id;
        private String schoolId;
        private String name;
    }

    @Data
    @Builder
    @JsonPropertyOrder({"id", "classId", "name"})
    public static class Student {
        private String id;
        private String classId;
        private String name;
    }

    @Builder
    @Value
    private static class Activity {
        private TableName tableName;
        private String sqlTableName;
        private Class<?> csvSchemaClass;
        private String csvFileName;
        private String deleteSqlStatement;
    }

    private static final Activity schoolsActivity = Activity.builder()
            .tableName(TableName.Schools)
            .sqlTableName("Schools")
            .csvSchemaClass(School.class)
            .csvFileName("schools.csv")
            .deleteSqlStatement("delete from Schools")
            .build();
    private static final Activity classesActivity = Activity.builder()
            .tableName(TableName.Classes)
            .sqlTableName("Classes")
            .csvSchemaClass(Klass.class)
            .csvFileName("classes.csv")
            .deleteSqlStatement("delete from Classes")
            .build();
    private static final Activity studentsActivity = Activity.builder()
            .tableName(TableName.Students)
            .sqlTableName("Students")
            .csvSchemaClass(Student.class)
            .csvFileName("students.csv")
            .deleteSqlStatement("delete from Students")
            .build();
}
