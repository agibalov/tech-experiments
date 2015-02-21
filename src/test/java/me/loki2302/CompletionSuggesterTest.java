package me.loki2302;

import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

// Completion suggester only looks for records which start with the search text
public class CompletionSuggesterTest extends ElasticSearchTest {
    @Test
    public void canGetCompletionSuggestions() throws IOException {
        // TODO: can it be not so ugly?
        client.admin().indices().prepareCreate("devices").execute().actionGet();

        client.admin().indices().preparePutMapping("devices").setType("device").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                            .startObject("device")
                                .startObject("properties")
                                    .startObject("name")
                                        .field("type", "completion")
                                        .field("payloads", false)
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject()
        ).execute().actionGet();

        client.prepareIndex("devices", "device", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "iphone")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "ipad")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "android")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

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

    private void checkSuggestions(String searchText, String[] expectedOptions) {
        SuggestResponse suggestResponse = client.prepareSuggest("devices")
                .addSuggestion(SuggestBuilders.completionSuggestion("nameSuggest")
                        .field("name")
                        .text(searchText))
                .execute().actionGet();

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
}
