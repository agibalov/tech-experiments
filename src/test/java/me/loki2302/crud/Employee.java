package me.loki2302.crud;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.HashSet;
import java.util.Set;

@Document(indexName = "employees", type = "employee")
public class Employee {
    @Id
    public String id;
    public String firstName;
    public String lastName;
    public int age;
    public String about;
    public Set<String> interests = new HashSet<>();
}
