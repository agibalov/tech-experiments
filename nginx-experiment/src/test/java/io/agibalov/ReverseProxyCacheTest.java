package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class ReverseProxyCacheTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("reverse-proxy-cache.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    @Test
    public void shouldNotCacheIfCacheControlIsPrivate() {
        wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Cache-Control", "private")
                .withBody("hello")));

        RestTemplate restTemplate = new RestTemplate();
        assertEquals("hello", restTemplate.getForObject("http://localhost", String.class));
        assertEquals("hello", restTemplate.getForObject("http://localhost", String.class));

        wireMockRule.verify(2, getRequestedFor(urlEqualTo("/")));
    }

    @Test
    public void shouldCacheIfCacheControlIsPublic() throws InterruptedException {
        wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Cache-Control", "public, max-age=1")
                .withBody("hello")));

        RestTemplate restTemplate = new RestTemplate();
        assertEquals("hello", restTemplate.getForObject("http://localhost", String.class));
        assertEquals("hello", restTemplate.getForObject("http://localhost", String.class));
        wireMockRule.verify(1, getRequestedFor(urlEqualTo("/")));
    }
}
