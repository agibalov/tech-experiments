package me.loki2302.search;

import me.loki2302.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
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
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AggregationTest {
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
    public void canGetAllInterests() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withTypes("employee")
                .addAggregation(AggregationBuilders.terms("allInterests").field("interests"))
                .build();
        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);

        StringTerms allInterestsStringTerms = searchResponse.getAggregations().get("allInterests");
        List<Terms.Bucket> buckets = allInterestsStringTerms.getBuckets();
        assertEquals(3, buckets.size());
        assertEquals("music", buckets.get(0).getKey());
        assertEquals(2, buckets.get(0).getDocCount());
        assertEquals("forestry", buckets.get(1).getKey());
        assertEquals(1, buckets.get(1).getDocCount());
        assertEquals("sports", buckets.get(2).getKey());
        assertEquals(1, buckets.get(2).getDocCount());
    }

    @Test
    public void canGetAverageAge() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withTypes("employee")
                .addAggregation(AggregationBuilders.avg("averageAge").field("age"))
                .build();
        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);

        Avg averageAge = searchResponse.getAggregations().get("averageAge");
        assertEquals(30.66, averageAge.getValue(), 0.01); // 30.6666666666666....
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
