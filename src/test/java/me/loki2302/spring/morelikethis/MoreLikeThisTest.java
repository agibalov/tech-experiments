package me.loki2302.spring.morelikethis;

import me.loki2302.spring.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
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
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MoreLikeThisTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private DeviceRepository deviceRepository;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
    }

    @Test
    public void canUsePhraseSuggester() throws IOException {
        deviceRepository.save(device("1", "apple iphone 6"));
        deviceRepository.save(device("2", "apple iphone 5"));
        deviceRepository.save(device("3", "google nexus 7"));

        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery()
                .like(new MoreLikeThisQueryBuilder.Item("devices", "device", "1"))
                .minTermFreq(1)
                .minDocFreq(1);

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(moreLikeThisQueryBuilder)
                .build();

        SearchResponse searchResponse = elasticsearchTemplate.query(nativeSearchQuery, response -> response);

        assertEquals(1, searchResponse.getHits().totalHits());
        assertEquals("2", searchResponse.getHits().getAt(0).getId());
    }

    private static Device device(String id, String name) {
        Device device = new Device();
        device.id = id;
        device.name = name;
        return device;
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
