package me.loki2302.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

@Component
public class ElasticsearchIntegrationTestUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchIntegrationTestUtils.class);

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public void reset() {
        elasticsearchOperations.deleteIndex("_all");

        Repositories repositories = new Repositories(context);
        StreamSupport.stream(repositories.spliterator(), false)
                .filter(domainClass -> {
                    boolean hasElasticsearchRepository =
                            repositories.getRepositoryFor(domainClass) instanceof ElasticsearchCrudRepository;
                    LOGGER.info("{} has elasticsearch repository: {}", domainClass, hasElasticsearchRepository);
                    return hasElasticsearchRepository;
                })
                .forEach(domainClass -> {
                    LOGGER.info("Creating index for {}", domainClass);
                    elasticsearchOperations.createIndex(domainClass);

                    LOGGER.info("Refreshing index for {}", domainClass);
                    elasticsearchOperations.refresh(domainClass);
                });
    }
}
