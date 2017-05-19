package me.loki2302.springrepositoriesrelations;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
    @Id
    public String id;
    public String name;
}
