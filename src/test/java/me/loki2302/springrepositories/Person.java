package me.loki2302.springrepositories;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Person {
    public String id;
    public String name;
}
