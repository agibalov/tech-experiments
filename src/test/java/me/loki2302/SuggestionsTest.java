package me.loki2302;

import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SuggestionsTest extends ElasticSearchTest {
    @Test
    public void canGetCompletionSuggestions() throws IOException {
        // TODO: can it be not so ugly?
        client.admin().indices().prepareCreate("notes").execute().actionGet();

        client.admin().indices().preparePutMapping("notes").setType("note").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                            .startObject("note")
                                .startObject("properties")
                                    .startObject("text")
                                        .field("type", "completion")
                                        .field("payloads", false)
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject()
        ).execute().actionGet();

        client.prepareIndex("notes", "note", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "iphone")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("notes", "note", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "ipad")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("notes", "note", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "android")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        SuggestResponse suggestResponse = client.prepareSuggest("notes")
                .addSuggestion(SuggestBuilders.completionSuggestion("textSuggest")
                        .field("text")
                        .text("i"))
                .execute().actionGet();

        Suggest.Suggestion suggestion = suggestResponse.getSuggest().getSuggestion("textSuggest");
        List<Suggest.Suggestion.Entry> entries = suggestion.getEntries();
        assertEquals(1, entries.size()); // TODO: what are these entries?

        Suggest.Suggestion.Entry entry = entries.get(0);
        List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
        assertEquals(2, options.size());
        assertEquals("ipad", options.get(0).getText().string());
        assertEquals("iphone", options.get(1).getText().string());
    }
}
