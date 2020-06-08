package io.agibalov;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestResult {
    private String testRunId;
    private String approach;
    private int numberOfSchools;
    private int numberOfClasses;
    private int numberOfStudents;
    private int numberOfRows;
    private float time;
}
