package me.loki2302;

import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PhraseSuggesterTest extends ElasticSearchTest {
    @Test
    public void canUsePhraseSuggester() throws IOException {
        client.prepareIndex("devices", "device", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "apple iphone 6")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "apple iphone 5")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "google nexus 7")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        SuggestResponse suggestResponse = client.prepareSuggest("devices")
                .addSuggestion(SuggestBuilders.phraseSuggestion("nameSuggest")
                    .field("name")
                    .text("aple"))
                .execute().actionGet();

        Suggest.Suggestion suggestion = suggestResponse.getSuggest().getSuggestion("nameSuggest");
        List<Suggest.Suggestion.Entry> entries = suggestion.getEntries();
        assertEquals(1, entries.size());

        Suggest.Suggestion.Entry entry = entries.get(0);
        List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
        assertEquals(1, options.size());
        assertEquals("apple", options.get(0).getText().string());
    }
}
