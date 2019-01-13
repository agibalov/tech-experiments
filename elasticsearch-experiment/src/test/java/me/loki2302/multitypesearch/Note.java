package me.loki2302.multitypesearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "notebook", type = "note")
public class Note {
    @Id
    public String id;
    public String text;
}
