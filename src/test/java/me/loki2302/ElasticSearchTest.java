package me.loki2302;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;

public abstract class ElasticSearchTest {
    protected Client client;
    private EmbeddedElasticSearch embeddedElasticSearch;

    @Before
    public void startElasticSearch() {
        embeddedElasticSearch = new EmbeddedElasticSearch();
        embeddedElasticSearch.start();

        client = embeddedElasticSearch.client();
        client.admin().indices().delete(new DeleteIndexRequest("_all")).actionGet();
    }

    @After
    public void stopElasticSearch() {
        client = null;
        embeddedElasticSearch.stop();
        embeddedElasticSearch = null;
    }
}
