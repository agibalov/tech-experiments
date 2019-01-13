package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ReverseProxyTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("reverse-proxy.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    @Test
    public void shouldProxyToLocalhost8080() {
        wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("hello")));

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://localhost", String.class);
        assertEquals("hello", result);

        wireMockRule.verify(getRequestedFor(urlEqualTo("/")));

        List<ServeEvent> theOnlyServeEvent = wireMockRule.getAllServeEvents();
        assertEquals(1, theOnlyServeEvent.size());
        assertNotEquals("", theOnlyServeEvent.get(0).getRequest().getHeader("X-My-Request-ID"));
        // X-My-Request-ID is something like "93b007e49ae84f4eb51b0306b1a19c97"
    }
}
