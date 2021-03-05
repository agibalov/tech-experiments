package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.oauth2ResourceServer().jwt();
            }
        };
    }

    @Bean
    public ApiController apiController() {
        return new ApiController();
    }

    @RequestMapping("/api")
    @ResponseBody
    public static class ApiController {
        @GetMapping("/hello")
        public ResponseEntity<?> hello(JwtAuthenticationToken token) {
            log.info("Token: {}", token.getTokenAttributes());
            return ResponseEntity.ok(Collections.singletonMap("message", String.format("Hello! %s", Instant.now())));
        }
    }
}
