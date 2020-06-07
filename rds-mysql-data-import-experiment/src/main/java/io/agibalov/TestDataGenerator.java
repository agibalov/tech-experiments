package io.agibalov;

import java.util.HashMap;
import java.util.Map;

public class TestDataGenerator {
    public void generate(TestDataWriter testDataWriter) {
        int schoolCount = 0;
        int classCount = 0;
        int studentCount = 0;
        for (int schoolIndex = 1; schoolIndex <= 10; ++schoolIndex) {
            String schoolId = String.format("school%d", schoolCount);
            Map<String, Object> schoolRow = new HashMap<>();
            schoolRow.put("id", schoolId);
            schoolRow.put("name", String.format("School %d", schoolCount));
            testDataWriter.write(TableName.Schools, schoolRow);
            ++schoolCount;

            for (int classIndex = 1; classIndex <= 10; ++classIndex) {
                String classId = String.format("class%d", classCount);
                Map<String, Object> classRow = new HashMap<>();
                classRow.put("id", classId);
                classRow.put("schoolId", schoolId);
                classRow.put("name", String.format("Class %d", classCount));
                testDataWriter.write(TableName.Classes, classRow);
                ++classCount;

                for (int studentIndex = 1; studentIndex <= 10; ++studentIndex) {
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
