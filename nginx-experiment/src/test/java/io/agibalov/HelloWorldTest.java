package io.agibalov;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

public class HelloWorldTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("hello-world.nginx")
            .withNetworkMode("host");

    @Test
    public void shouldRespondWithHelloWorld() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://localhost", String.class);
        assertEquals("hello there!", result);
    }
}
