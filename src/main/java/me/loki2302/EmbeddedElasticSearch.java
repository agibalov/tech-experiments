package me.loki2302;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class EmbeddedElasticSearch {
    private String dataDirectory;
    private Node node;

    public void start() {
        if(node != null) {
            throw new IllegalStateException();
        }

        dataDirectory = Paths.get("data", UUID.randomUUID().toString()).toString();
        Settings settings = Settings.settingsBuilder()
                .put("http.enabled", false)
                .put("path.home", System.getProperty("user.dir"))
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
        node = null;

        try {
            FileUtils.deleteDirectory(Paths.get(dataDirectory).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dataDirectory = null;
    }

    public Client client() {
        if(node == null) {
            throw new IllegalStateException();
        }

        return node.client();
    }
}
