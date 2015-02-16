package me.loki2302;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

public class EmployeesDataset {
    public static void populate(Client client) throws IOException {
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

        client.prepareIndex("megacorp", "employee", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "Jane")
                        .field("lastName", "Smith")
                        .field("age", 32)
                        .field("about", "I like to collect rock albums")
                        .array("interests", "music")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("megacorp", "employee", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("firstName", "Douglas")
                        .field("lastName", "Fir")
                        .field("age", 35)
                        .field("about", "I like to build cabinets")
                        .array("interests", "forestry")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();
    }
}
