package me.loki2302.springrepositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface BookRepository extends ElasticsearchCrudRepository<Book, String> {
}
