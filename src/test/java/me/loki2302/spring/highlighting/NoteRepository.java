package me.loki2302.spring.highlighting;

import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface NoteRepository extends ElasticsearchCrudRepository<Note, String> {
}
