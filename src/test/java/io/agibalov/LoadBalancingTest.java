package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class LoadBalancingTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("load-balancer.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule1 = new WireMockRule(8081);

    @Rule
    public final WireMockRule wireMockRule2 = new WireMockRule(8082);

    @Test
    public void shouldProxyToLocalhost8080() {
        wireMockRule1.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("hello1")));

        wireMockRule2.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("hello2")));

        RestTemplate restTemplate = new RestTemplate();

        Set<String> actualResponses = new HashSet<>();
        for(int i = 0; i < 2; ++i) {
            String result = restTemplate.getForObject("http://localhost", String.class);
            actualResponses.add(result);
        }

        assertEquals(new HashSet<>(Arrays.asList("hello1", "hello2")), actualResponses);

        wireMockRule1.verify(getRequestedFor(urlEqualTo("/")));
        wireMockRule2.verify(getRequestedFor(urlEqualTo("/")));
    }
}
