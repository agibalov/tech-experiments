package me.loki2302.dummy;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "books", type = "book")
public class Book {
    @Id
    public String id;
    public String title;
}
