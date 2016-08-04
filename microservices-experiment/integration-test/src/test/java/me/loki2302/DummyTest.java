package me.loki2302;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
public class DummyTest {
    @Autowired
    private AddServiceClient addServiceClient;

    @Autowired
    private SubServiceClient subServiceClient;

    @Autowired
    private CalcServiceClient calcServiceClient;

    @Test
    public void addServiceTest() {
        assertEquals(5, addServiceClient.add(2, 3));
    }

    @Test
    public void subServiceTest() {
        assertEquals(-1, subServiceClient.sub(2, 3));
    }

    @Test
    public void calcServiceAddTest() {
        assertEquals(5, calcServiceClient.add(2, 3));
    }

    @Test
    public void calcServiceSubTest() {
        assertEquals(-1, calcServiceClient.sub(2, 3));
    }

    @Configuration
    @EnableAutoConfiguration
    public static class Config {
        @Bean
        public AddServiceClient addServiceClient(@Value("${ADD_SERVICE_URL}") String addServiceUrl) {
            return new AddServiceClient(addServiceUrl);
        }

        @Bean
        public SubServiceClient subServiceClient(@Value("${SUB_SERVICE_URL}") String subServiceUrl) {
            return new SubServiceClient(subServiceUrl);
        }

        @Bean
        public CalcServiceClient calcServiceClient(@Value("${CALC_SERVICE_URL}") String calcServiceUrl) {
            return new CalcServiceClient(calcServiceUrl);
        }
    }

    public static class AddServiceClient {
        private final String apiRootUrl;
        private final RestTemplate restTemplate = new RestTemplate();

        public AddServiceClient(String apiRootUrl) {
            this.apiRootUrl = apiRootUrl;
        }

        public int add(int a, int b) {
            String url = fromHttpUrl(apiRootUrl).path("/add/{a}/{b}").buildAndExpand(a, b).toUriString();
            ResponseDTO responseDto = restTemplate.getForObject(url, ResponseDTO.class);
            return responseDto.result;
        }
    }

    public static class SubServiceClient {
        private final String apiRootUrl;
        private final RestTemplate restTemplate = new RestTemplate();

        public SubServiceClient(String apiRootUrl) {
            this.apiRootUrl = apiRootUrl;
        }

        public int sub(int a, int b) {
            String url = fromHttpUrl(apiRootUrl).path("/sub/{a}/{b}").buildAndExpand(a, b).toUriString();
            ResponseDTO responseDto = restTemplate.getForObject(url, ResponseDTO.class);
            return responseDto.result;
        }
    }

    public static class CalcServiceClient {
        private final String apiRootUrl;
        private final RestTemplate restTemplate = new RestTemplate();

        public CalcServiceClient(String apiRootUrl) {
            this.apiRootUrl = apiRootUrl;
        }

        public int add(int a, int b) {
            String url = fromHttpUrl(apiRootUrl).path("/add/{a}/{b}").buildAndExpand(a, b).toUriString();
            ResponseDTO responseDto = restTemplate.getForObject(url, ResponseDTO.class);
            return responseDto.result;
        }

        public int sub(int a, int b) {
            String url = fromHttpUrl(apiRootUrl).path("/sub/{a}/{b}").buildAndExpand(a, b).toUriString();
            ResponseDTO responseDto = restTemplate.getForObject(url, ResponseDTO.class);
            return responseDto.result;
        }
    }

    public static class ResponseDTO {
        public int a;
        public int b;
        public int result;
    }
}
