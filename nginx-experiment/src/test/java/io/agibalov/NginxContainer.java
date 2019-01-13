package io.agibalov;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NginxContainer<SELF extends NginxContainer<SELF>> extends GenericContainer<SELF> {
    public NginxContainer() {
        super("nginx:1.13.10");
    }

    public NginxContainer<SELF> withConfig(String localConfigFilename) {
        addFileSystemBind(localConfigFilename, "/etc/nginx/nginx.conf", BindMode.READ_ONLY);
        return this;
    }

    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        return new HashSet<>(Arrays.asList(80));
    }

    @Override
    protected void configure() {
        setCommand("nginx", "-g", "daemon off;");
    }
}
