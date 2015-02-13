package me.loki2302;

import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;

public class ElasticSearchTest {
    protected Client client;
    private EmbeddedElasticSearch embeddedElasticSearch;

    @Before
    public void startElasticSearch() {
        embeddedElasticSearch = new EmbeddedElasticSearch();
        embeddedElasticSearch.start();

        client = embeddedElasticSearch.client();
    }

    @After
    public void stopElasticSearch() {
        client = null;
        embeddedElasticSearch.stop();
        embeddedElasticSearch = null;
    }
}
