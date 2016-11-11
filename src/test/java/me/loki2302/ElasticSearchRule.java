package me.loki2302;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ElasticSearchRule implements TestRule {
    public EmbeddedElasticSearch embeddedElasticSearch = new EmbeddedElasticSearch();
    public Client client;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                embeddedElasticSearch = new EmbeddedElasticSearch();
                embeddedElasticSearch.start();

                client = embeddedElasticSearch.client();
                client.admin().indices().delete(new DeleteIndexRequest("_all")).actionGet();

                try {
                    base.evaluate();
                } finally {
                    client = null;
                    embeddedElasticSearch.stop();
                    embeddedElasticSearch = null;
                }
            }
        };
    }
}
