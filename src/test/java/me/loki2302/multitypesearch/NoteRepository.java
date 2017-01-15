package me.loki2302.multitypesearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NoteRepository extends ElasticsearchRepository<Note, String> {
}
