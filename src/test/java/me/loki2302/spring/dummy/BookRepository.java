package me.loki2302.spring.dummy;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookRepository extends ElasticsearchRepository<Book, String> {
}
