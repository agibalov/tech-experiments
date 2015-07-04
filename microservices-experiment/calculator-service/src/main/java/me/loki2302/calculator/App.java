package me.loki2302.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties
public class App {
    // --calc.add_service_url=...
    // --calc.sub_service_url=...
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Component
    @ConfigurationProperties("calc")
    public static class CalculatorProperties {
        private final static Logger log = LoggerFactory.getLogger(CalculatorProperties.class);

        private String addServiceUrl;
        private String subServiceUrl;

        public String getAddServiceUrl() {
            return addServiceUrl;
        }

        public void setAddServiceUrl(String addServiceUrl) {
            this.addServiceUrl = addServiceUrl;
        }

        public String getSubServiceUrl() {
            return subServiceUrl;
        }

        public void setSubServiceUrl(String subServiceUrl) {
            this.subServiceUrl = subServiceUrl;
        }

        @PostConstruct
        public void dump() {
            log.info("addServiceUrl: {}", addServiceUrl);
            log.info("subServiceUrl: {}", subServiceUrl);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RestController
    public static class ApiController {
        private final static Logger log = LoggerFactory.getLogger(ApiController.class);

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private CalculatorProperties calculatorProperties;

        @RequestMapping(value = "/add/{a}/{b}", method = RequestMethod.GET)
        public ResponseDTO addNumbers(
                @PathVariable("a") int a,
                @PathVariable("b") int b) {

            String addServiceUrl = calculatorProperties.getAddServiceUrl();
            String requestUrl = UriComponentsBuilder.fromUriString(addServiceUrl)
                    .path("/add/{a}/{b}")
                    .buildAndExpand(a, b)
                    .toUriString();

            log.info("Request URL: {}", requestUrl);

            ResponseDTO responseDTO = restTemplate.getForObject(
                    requestUrl, ResponseDTO.class);

            return responseDTO;
        }

        @RequestMapping(value = "/sub/{a}/{b}", method = RequestMethod.GET)
        public ResponseDTO subNumbers(
                @PathVariable("a") int a,
                @PathVariable("b") int b) {

            String subServiceUrl = calculatorProperties.getSubServiceUrl();
            String requestUrl = UriComponentsBuilder.fromUriString(subServiceUrl)
                    .path("/sub/{a}/{b}")
                    .buildAndExpand(a, b)
                    .toUriString();

            log.info("Request template: {}", requestUrl);

            ResponseDTO responseDTO = restTemplate.getForObject(
                    requestUrl, ResponseDTO.class);

            return responseDTO;
        }
    }

    public static class ResponseDTO {
        public int a;
        public int b;
        public int result;
    }
}
