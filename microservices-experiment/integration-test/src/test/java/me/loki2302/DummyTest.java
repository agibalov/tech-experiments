package me.loki2302;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest
@SpringApplicationConfiguration
public class DummyTest {
    @Value("${ADD_SERVICE_URL}")
    private String addServiceUrl;

    @Value("${SUB_SERVICE_URL}")
    private String subServiceUrl;

    @Value("${CALC_SERVICE_URL}")
    private String calcServiceUrl;

    @Test
    public void addServiceTest() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseDTO responseDto = restTemplate.getForObject(
                addServiceUrl + "/add/{a}/{b}",
                ResponseDTO.class,
                2,
                3);

        assertEquals(2, responseDto.a);
        assertEquals(3, responseDto.b);
        assertEquals(5, responseDto.result);
    }

    @Test
    public void subServiceTest() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseDTO responseDto = restTemplate.getForObject(
                subServiceUrl + "/sub/{a}/{b}",
                ResponseDTO.class,
                2,
                3);

        assertEquals(2, responseDto.a);
        assertEquals(3, responseDto.b);
        assertEquals(-1, responseDto.result);
    }

    @Test
    public void calcServiceAddTest() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseDTO responseDto = restTemplate.getForObject(
                calcServiceUrl + "/add/{a}/{b}",
                ResponseDTO.class,
                2,
                3);

        assertEquals(2, responseDto.a);
        assertEquals(3, responseDto.b);
        assertEquals(5, responseDto.result);
    }

    @Test
    public void calcServiceSubTest() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseDTO responseDto = restTemplate.getForObject(
                calcServiceUrl + "/sub/{a}/{b}",
                ResponseDTO.class,
                2,
                3);

        assertEquals(2, responseDto.a);
        assertEquals(3, responseDto.b);
        assertEquals(-1, responseDto.result);
    }

    @Configuration
    @EnableAutoConfiguration
    public static class Config {
    }

    public static class ResponseDTO {
        public int a;
        public int b;
        public int result;
    }
}
