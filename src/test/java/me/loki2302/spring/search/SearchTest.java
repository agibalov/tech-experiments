package me.loki2302.spring.search;

import me.loki2302.spring.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
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

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
        EmployeesDataset2.populate(employeeRepository);
    }

    @Test
    public void canSearchEmployeesWithLastNameTermQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // TODO: quite weird. When I save a value like "Hello-There",
                // TODO: the index is built for "hello" and "there",
                // TODO: so in case of "Smith", I only can search by "smith" :-/
                .withQuery(QueryBuilders.termQuery("lastName", "smith"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithLastNameMatchQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // as opposed to termQuery, this will first convert "Smith" to "smith"
                .withQuery(QueryBuilders.matchQuery("lastName", "Smith"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAgeTermQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("age", 35))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(1, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAgeRangeQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.rangeQuery("age").from(32))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAboutMatchQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // this will look for "rock" and "climbing", not for "rock climbing"
                .withQuery(QueryBuilders.matchQuery("about", "rock climbing"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAboutMatchPhraseQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // as opposed to matchQuery, this will look for "rock climbing"
                .withQuery(QueryBuilders.matchPhraseQuery("about", "rock climbing"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(1, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithOneInterestTermQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("interests", "music"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithTwoInterestTermsQuery() {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // at least one of "music" and "sports", not necessarily both
                .withQuery(QueryBuilders.termsQuery("interests", "music", "sports"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);
        assertEquals(2, searchResponse.getHits().totalHits());
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
