package me.loki2302.morelikethis;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "devices", type = "device")
public class Device {
    @Id
    public String id;
    public String name;
}
