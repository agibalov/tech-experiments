package me.loki2302.suggestions;

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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TermSuggesterTest {
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
    public void canGetTermSuggestions() throws IOException {
        deviceRepository.save(device("1", "Apple iPhone"));
        deviceRepository.save(device("2", "Apple iPad"));
        deviceRepository.save(device("3", "HTC Tattoo"));

        checkSuggestions("a", new String[]{});
        checkSuggestions("ap", new String[]{});
        checkSuggestions("app", new String[]{});
        checkSuggestions("appl", new String[]{"apple"});
        checkSuggestions("apple", new String[]{});
        checkSuggestions("aple", new String[]{"apple"});

        checkSuggestions("iphone", new String[]{});
        checkSuggestions("iphane", new String[]{"iphone"});
        checkSuggestions("iphne", new String[]{"iphone"});
        checkSuggestions("ipne", new String[]{"ipad", "iphone"});

        checkSuggestions("tatu", new String[]{});
        checkSuggestions("tatoo", new String[]{"tattoo"});
        checkSuggestions("tattu", new String[]{"tattoo"});
    }

    private static Device device(String id, String name) {
        Device device = new Device();
        device.id = id;
        device.name = name;
        return device;
    }

    private void checkSuggestions(String searchText, String[] expectedOptions) {
        SuggestResponse suggestResponse = elasticsearchTemplate.suggest(SuggestBuilders.termSuggestion("nameSuggest")
                .field("name")
                .text(searchText));

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
