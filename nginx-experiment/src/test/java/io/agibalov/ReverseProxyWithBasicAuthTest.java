package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;

import java.io.IOException;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class ReverseProxyWithBasicAuthTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer() {{
        addFileSystemBind("nginx-htpasswd", "/etc/nginx/nginx-htpasswd", BindMode.READ_ONLY);
    }}
            .withConfig("reverse-proxy-with-basic-auth.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(8080);

    @Test
    public void shouldRequireAuthorizationHeader() {
        wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("here is the secret")));

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
        assertEquals(HttpStatus.UNAUTHORIZED, result1.getStatusCode());
        wireMockRule.verify(0, getRequestedFor(urlEqualTo("/")));

        RequestEntity<?> requestEntity = RequestEntity.get(URI.create("http://localhost"))
                .header("Authorization", "Basic " + Base64Utils.encodeToString("user1:qwerty".getBytes()))
                .build();
        ResponseEntity<String> result2 = restTemplate.exchange(requestEntity, String.class);
        assertEquals("here is the secret", result2.getBody());
    }
}
