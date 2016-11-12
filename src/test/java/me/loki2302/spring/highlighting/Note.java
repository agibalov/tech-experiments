package me.loki2302.spring.highlighting;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "notes", type = "note")
public class Note {
    @Id
    public String id;
    public String text;
}
