package me.loki2302.spring.search;

import me.loki2302.spring.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchPaginationTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
        EmployeesDataset.populate(employeeRepository);
    }

    @Test
    public void canGetYoungestEmployeeUsingRepository() {
        Page<Employee> page = employeeRepository.findAll(new PageRequest(0, 1, Sort.Direction.ASC, "age"));

        List<Employee> pageContent = page.getContent();
        assertEquals(1, pageContent.size());

        Employee theOnlyEmployee = pageContent.get(0);
        assertEquals("1", theOnlyEmployee.id);
        assertEquals(25, theOnlyEmployee.age);
    }

    @Test
    public void canGetYoungestEmployeeUsingOperations() {
        employeeRepository.findAll(new PageRequest(0, 1, Sort.Direction.ASC, "age"));

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withTypes("employee")
                .withQuery(QueryBuilders.matchAllQuery())
                .withSort(SortBuilders.fieldSort("age").order(SortOrder.ASC))
                .withPageable(new PageRequest(0, 1)).build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, x -> x);

        SearchHit searchHit = searchResponse.getHits().getAt(0);
        assertEquals("1", searchHit.getId());

        Map<String, Object> source = searchHit.getSource();
        assertEquals(25, (int)(Integer)source.get("age"));
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
