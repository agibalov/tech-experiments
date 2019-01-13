package io.agibalov;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class ReverseProxyVirtualHostsTest {
    @Rule
    public final NginxContainer nginxContainer = (NginxContainer) new NginxContainer()
            .withConfig("reverse-proxy-virtual-hosts.nginx")
            .withNetworkMode("host");

    @Rule
    public final WireMockRule wireMockRule1 = new WireMockRule(8081);

    @Rule
    public final WireMockRule wireMockRule2 = new WireMockRule(8082);

    @Test
    public void shouldRespondDifferentlyDependingOnHostHeader() {
        wireMockRule1.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("I am host1")));

        wireMockRule2.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(200)
                .withBody("I am host2")));

        {
            HttpClient httpClient = HttpClients.custom()
                    .addInterceptorFirst((HttpRequestInterceptor) (request, context) ->
                            request.setHeader("Host", "hosta"))
                    .build();

            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
            String result = restTemplate.getForObject("http://localhost", String.class);
            assertEquals("I am host1", result);
        }

        {
            HttpClient httpClient = HttpClients.custom()
                    .addInterceptorFirst((HttpRequestInterceptor) (request, context) ->
                            request.setHeader("Host", "hostb"))
                    .build();

            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
            String result = restTemplate.getForObject("http://localhost", String.class);
            assertEquals("I am host2", result);
        }
    }
}
