package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReverseProxyRateLimitsTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("reverse-proxy-rate-limits.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    @Test
    public void shoulRateLimit() throws InterruptedException {
        wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("hello")));

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
            }
        });

        ResponseEntity<String> result1 = restTemplate.getForEntity("http://localhost", String.class);
        assertEquals(HttpStatus.OK, result1.getStatusCode());
        assertEquals("hello", result1.getBody());

        ResponseEntity<String> result2 = restTemplate.getForEntity("http://localhost", String.class);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result2.getStatusCode());
        assertTrue(result2.getBody().contains("503 Service Temporarily Unavailable"));

        wireMockRule.verify(1, getRequestedFor(urlEqualTo("/")));

        Thread.sleep(1000);

        ResponseEntity<String> result3 = restTemplate.getForEntity("http://localhost", String.class);
        assertEquals(HttpStatus.OK, result3.getStatusCode());
        assertEquals("hello", result3.getBody());
    }
}
