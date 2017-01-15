package me.loki2302.morelikethis;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DeviceRepository extends ElasticsearchRepository<Device, String> {
}
