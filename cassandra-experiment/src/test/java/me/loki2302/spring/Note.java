package me.loki2302.spring;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

@Table
public class Note {
    @PrimaryKey
    public UUID id;
    public String content;
}
