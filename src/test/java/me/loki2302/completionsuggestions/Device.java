package me.loki2302.completionsuggestions;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;

@Document(indexName = "devices", type = "device")
public class Device {
    @Id
    public String id;
    public String name;

    @CompletionField
    public Completion suggest;
}
