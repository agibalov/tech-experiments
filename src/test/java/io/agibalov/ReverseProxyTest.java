package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

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
        verify(getRequestedFor(urlEqualTo("/")));
    }
}
