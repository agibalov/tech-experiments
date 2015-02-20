package me.loki2302;

import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TermSuggesterTest extends ElasticSearchTest {
    @Test
    public void canGetTermSuggestions() throws IOException {
        client.prepareIndex("devices", "device", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "Apple iPhone")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "Apple iPad")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "HTC Tattoo")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

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

    private void checkSuggestions(String searchText, String[] expectedOptions) {
        SuggestResponse suggestResponse = client.prepareSuggest("devices")
                .addSuggestion(SuggestBuilders.termSuggestion("nameSuggest")
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
