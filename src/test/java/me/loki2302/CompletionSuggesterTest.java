package me.loki2302;

import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

        if(true) {
            SuggestResponse suggestResponse = client.prepareSuggest("devices")
                    .addSuggestion(SuggestBuilders.completionSuggestion("nameSuggest")
                            .field("name")
                            .text("i"))
                    .execute().actionGet();

            Suggest.Suggestion suggestion = suggestResponse.getSuggest().getSuggestion("nameSuggest");
            List<Suggest.Suggestion.Entry> entries = suggestion.getEntries();
            assertEquals(1, entries.size()); // TODO: what are these entries?

            Suggest.Suggestion.Entry entry = entries.get(0);
            List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
            assertEquals(2, options.size());
            assertEquals("ipad", options.get(0).getText().string());
            assertEquals("iphone", options.get(1).getText().string());
        }

        if(true) {
            SuggestResponse suggestResponse = client.prepareSuggest("devices")
                    .addSuggestion(SuggestBuilders.completionSuggestion("nameSuggest")
                            .field("name")
                            .text("and"))
                    .execute().actionGet();

            Suggest.Suggestion suggestion = suggestResponse.getSuggest().getSuggestion("nameSuggest");
            List<Suggest.Suggestion.Entry> entries = suggestion.getEntries();
            assertEquals(1, entries.size()); // TODO: what are these entries?

            Suggest.Suggestion.Entry entry = entries.get(0);
            List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
            assertEquals(1, options.size());
            assertEquals("android", options.get(0).getText().string());
        }
    }
}
