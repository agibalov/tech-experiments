package me.loki2302.suggestions;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DeviceRepository extends ElasticsearchRepository<Device, String> {
}
