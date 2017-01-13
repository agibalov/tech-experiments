package me.loki2302.spring.search;

import java.io.IOException;
import java.util.Arrays;

public class EmployeesDataset {
    public static void populate(EmployeeRepository employeeRepository) throws IOException {
        if(true) {
            Employee employee = new Employee();
            employee.id = "1";
            employee.firstName = "John";
            employee.lastName = "Smith";
            employee.age = 25;
            employee.about = "I love to go rock climbing";
            employee.interests.addAll(Arrays.asList("sports", "music"));
            employeeRepository.save(employee);
        }

        if(true) {
            Employee employee = new Employee();
            employee.id = "2";
            employee.firstName = "Jane";
            employee.lastName = "Smith";
            employee.age = 32;
            employee.about = "I like to collect rock albums";
            employee.interests.addAll(Arrays.asList("music"));
            employeeRepository.save(employee);
        }

        if(true) {
            Employee employee = new Employee();
            employee.id = "3";
            employee.firstName = "Douglas";
            employee.lastName = "Fir";
            employee.age = 35;
            employee.about = "I like to build cabinets";
            employee.interests.addAll(Arrays.asList("forestry"));
            employeeRepository.save(employee);
        }
    }
}
