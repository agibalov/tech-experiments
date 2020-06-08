package io.agibalov;

import java.util.HashMap;
import java.util.Map;

public class TestDataGenerator {
    private final int numberOfSchools;
    private final int numberOfClasses;
    private final int numberOfStudents;

    public TestDataGenerator(int numberOfSchools, int numberOfClasses, int numberOfStudents) {
        this.numberOfSchools = numberOfSchools;
        this.numberOfClasses = numberOfClasses;
        this.numberOfStudents = numberOfStudents;
    }

    public void generate(TestDataWriter testDataWriter) {
        int schoolCount = 0;
        int classCount = 0;
        int studentCount = 0;
        for (int schoolIndex = 1; schoolIndex <= numberOfSchools; ++schoolIndex) {
            String schoolId = String.format("school%d", schoolCount);
            Map<String, Object> schoolRow = new HashMap<>();
            schoolRow.put("id", schoolId);
            schoolRow.put("name", String.format("School %d", schoolCount));
            testDataWriter.write(TableName.Schools, schoolRow);
            ++schoolCount;

            for (int classIndex = 1; classIndex <= numberOfClasses; ++classIndex) {
                String classId = String.format("class%d", classCount);
                Map<String, Object> classRow = new HashMap<>();
                classRow.put("id", classId);
                classRow.put("schoolId", schoolId);
                classRow.put("name", String.format("Class %d", classCount));
                testDataWriter.write(TableName.Classes, classRow);
                ++classCount;

                for (int studentIndex = 1; studentIndex <= numberOfStudents; ++studentIndex) {
                    String studentId = String.format("student%d", studentCount);
                    Map<String, Object> studentRow = new HashMap<>();
                    studentRow.put("id", studentId);
                    studentRow.put("classId", classId);
                    studentRow.put("name", String.format("Student %d", studentCount));
                    testDataWriter.write(TableName.Students, studentRow);
                    ++studentCount;
                }
            }
        }
    }
}
