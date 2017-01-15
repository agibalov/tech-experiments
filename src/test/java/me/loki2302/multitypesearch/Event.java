package me.loki2302.multitypesearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "notebook", type = "event")
public class Event {
    @Id
    public String id;
    public String text;
}
