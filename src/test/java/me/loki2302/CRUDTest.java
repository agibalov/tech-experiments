package me.loki2302;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CRUDTest extends ElasticSearchTest {
    @Test
    public void canCreateAnEmployee() throws IOException {
        IndexResponse indexResponse = client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        assertTrue(indexResponse.isCreated());
        assertEquals(1, indexResponse.getVersion());
    }

    @Test
    public void canGetEmployeeById() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        GetResponse getResponse = client.prepareGet("megacorp", "employee", "1")
                .execute().actionGet();
        assertTrue(getResponse.isExists());
        assertEquals(1, getResponse.getVersion());

        Map<String, Object> source = getResponse.getSource();
        assertEquals(source.get("firstName"), "John");
        assertEquals(source.get("lastName"), "Smith");
        assertEquals(source.get("age"), 25);
        assertEquals(source.get("about"), "I love to go rock climbing");

        List<String> interests = (List)source.get("interests");
        assertEquals(2, interests.size());
        assertTrue(interests.contains("sports"));
        assertTrue(interests.contains("music"));
    }

    @Test
    public void cantGetEmployeeIfItDoesNotExist() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        GetResponse getResponse = client.prepareGet("megacorp", "employee", "123")
                .execute().actionGet();
        assertFalse(getResponse.isExists());
    }

    @Test
    public void canUpdateEmployee() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        IndexResponse indexResponse = client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I hate to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .execute().actionGet();

        assertFalse(indexResponse.isCreated());
        assertEquals(2, indexResponse.getVersion());
    }

    @Test
    public void canUpdateEmployeeWithVersionControl() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        IndexResponse indexResponse = client.prepareIndex("megacorp", "employee", "1")
                .setVersion(1)
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I hate to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        assertFalse(indexResponse.isCreated());
        assertEquals(2, indexResponse.getVersion());
    }

    @Test
    public void cantUpdateEmployeeWithVersionControlIfVersionIsNotOk() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        IndexResponse updateOneIndexResponse = client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I hate to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();
        assertEquals(2, updateOneIndexResponse.getVersion());

        try {
            client.prepareIndex("megacorp", "employee", "1")
                    .setVersion(1)
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("firstName", "John")
                            .field("lastName", "Smith")
                            .field("age", 25)
                            .field("about", "I ... to go rock climbing")
                            .array("interests", "sports", "music")
                            .endObject())
                    .setRefresh(true)
                    .execute().actionGet();

            fail();
        } catch (VersionConflictEngineException e) {
            assertEquals(2, e.getCurrentVersion());
            assertEquals(1, e.getProvidedVersion());
        }
    }

    @Test
    public void canDeleteAnEmployee() throws IOException {
        client.prepareIndex("megacorp", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "John")
                        .field("lastName", "Smith")
                        .field("age", 25)
                        .field("about", "I love to go rock climbing")
                        .array("interests", "sports", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        DeleteResponse deleteResponse = client.prepareDelete("megacorp", "employee", "1")
                .execute().actionGet();
        assertTrue(deleteResponse.isFound());
        assertEquals(2, deleteResponse.getVersion());
    }
}
