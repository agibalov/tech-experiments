package me.loki2302.spring.dummy;

import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface BookRepository extends ElasticsearchCrudRepository<Book, String> {
}
