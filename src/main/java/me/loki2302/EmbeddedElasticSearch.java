package me.loki2302;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;

public class EmbeddedElasticSearch {
    private final static String dataDirectory = "1";
    private Node node;

    public void start() {
        if(node != null) {
            throw new IllegalStateException();
        }

        Settings settings = ImmutableSettings.builder()
                .put("http.enabled", false)
                .put("path.data", dataDirectory)
                .build();

        node = new NodeBuilder()
                .local(true)
                .settings(settings)
                .node();
    }

    public void stop() {
        if(node == null) {
            throw new IllegalStateException();
        }

        node.close();
        FileSystemUtils.deleteRecursively(new File(dataDirectory));

        node = null;
    }

    public Client client() {
        if(node == null) {
            throw new IllegalStateException();
        }

        return node.client();
    }
}
