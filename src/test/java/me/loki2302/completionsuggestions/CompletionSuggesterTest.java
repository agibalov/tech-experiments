package me.loki2302.completionsuggestions;

import me.loki2302.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
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
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CompletionSuggesterTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
    }

    @Test
    public void canGetCompletionSuggestions() throws IOException {
        elasticsearchTemplate.deleteIndex(Device.class);
        elasticsearchTemplate.createIndex(Device.class);
        elasticsearchTemplate.refresh(Device.class);
        elasticsearchTemplate.putMapping(Device.class);

        Device device1 = device("1", "iphone");
        Device device2 = device("2", "ipad");
        Device device3 = device("3", "android");

        List<IndexQuery> indexQueries = new ArrayList<>();
        for(Device device : new Device[] { device1, device2, device3 }) {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(device.id);
            indexQuery.setObject(device);
            indexQueries.add(indexQuery);
        }

        elasticsearchTemplate.bulkIndex(indexQueries);
        elasticsearchTemplate.refresh(Device.class);

        checkSuggestions("i", new String[] {"ipad", "iphone"});
        checkSuggestions("ip", new String[] {"ipad", "iphone"});
        checkSuggestions("ipa", new String[] {"ipad"});
        checkSuggestions("ipad", new String[] {"ipad"});
        checkSuggestions("iph", new String[] {"iphone"});
        checkSuggestions("ipho", new String[] {"iphone"});
        checkSuggestions("iphon", new String[] {"iphone"});
        checkSuggestions("iphone", new String[] {"iphone"});
        checkSuggestions("and", new String[] {"android"});
    }

    private static Device device(String id, String name) {
        Device device = new Device();
        device.id = id;
        device.name = name;

        Completion completion = new Completion(new String[] { name });
        device.suggest = completion;

        return device;
    }

    private void checkSuggestions(String searchText, String[] expectedOptions) {
        SuggestResponse suggestResponse = elasticsearchTemplate.suggest(SuggestBuilders.completionSuggestion("nameSuggest")
                .field("suggest")
                .text(searchText), Device.class);

        Suggest.Suggestion suggestion = suggestResponse.getSuggest().getSuggestion("nameSuggest");
        List<Suggest.Suggestion.Entry> entries = suggestion.getEntries();
        assertEquals(1, entries.size());

        Suggest.Suggestion.Entry entry = entries.get(0);
        List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();

        int expectedNumberOfOptions = expectedOptions.length;
        assertEquals(expectedNumberOfOptions, options.size());

        for(int i = 0; i < expectedNumberOfOptions; ++i) {
            String expectedOption = expectedOptions[i];
            String actualOption = options.get(i).getText().string();
            assertEquals(expectedOption, actualOption);
        }
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
