package me.loki2302.crud;

import me.loki2302.ElasticsearchIntegrationTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CrudTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
    }

    @Test
    public void canCreateAndGetAnEmployee() {
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

        Employee employee = employeeRepository.findOne("1");
        assertEquals("1", employee.id);
        assertEquals("John", employee.firstName);
        assertEquals("Smith", employee.lastName);
        assertEquals(25, employee.age);
        assertEquals("I love to go rock climbing", employee.about);
        assertEquals(2, employee.interests.size());
        assertTrue(employee.interests.contains("sports"));
        assertTrue(employee.interests.contains("music"));
    }

    @Test
    public void cantGetEmployeeIfItDoesNotExist() {
        Employee employee = employeeRepository.findOne("123");
        assertNull(employee);
    }

    @Test
    public void canUpdateAnEmployee() {
        if(true) {
            Employee employee = new Employee();
            employee.id = "1";
            employee.firstName = "John";
            employee.lastName = "Smith";
            employee.age = 25;
            employee.about = "I love to go rock climbing";
            employee.interests.addAll(Arrays.asList("sports", "music"));
            employee = employeeRepository.save(employee);

            employee.age = 26;
            employeeRepository.save(employee);
        }

        Employee employee = employeeRepository.findOne("1");
        assertEquals("1", employee.id);
        assertEquals("John", employee.firstName);
        assertEquals("Smith", employee.lastName);
        assertEquals(26, employee.age);
        assertEquals("I love to go rock climbing", employee.about);
        assertEquals(2, employee.interests.size());
        assertTrue(employee.interests.contains("sports"));
        assertTrue(employee.interests.contains("music"));
    }

    @Test
    public void canDeleteAnEmployee() {
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

        employeeRepository.delete("1");

        Employee employee = employeeRepository.findOne("123");
        assertNull(employee);
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan
    public static class Config {
        @Bean
        public ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils() {
            return new ElasticsearchIntegrationTestUtils();
        }
    }
}
