package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String[] args) {
        new SpringApplicationBuilder(App.class).run(args);
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tracingValveWebServerFactoryCustomizer() {
        return factory -> factory.addEngineValves(new RequestTracingValve());
    }

    @RestController
    @Slf4j
    public static class DummyController {
        private final Random random = new Random();

        @GetMapping("/hello")
        public ResponseEntity hello() {
            log.info("hello!");
            return ResponseEntity.ok(String.format("hello %s", Instant.now()));
        }

        @GetMapping("/crash")
        public ResponseEntity crash() {
            log.info("this\nis\na\nmultiline\nlog\nmessage... And we're about to crash!");
            throw new RuntimeException("Something bad!");
        }

        @Scheduled(fixedRate = 1000)
        public void ping() {
            if (random.nextBoolean()) {
                log.info("Current time is {}", Instant.now());
            } else {
                throw new RuntimeException("Failure!!!11");
            }
        }
    }

    @Component
    public static class Dummy implements CommandLineRunner {
        private final static Logger logger = LoggerFactory.getLogger(Dummy.class);

        @Override
        public void run(String... args) {
            MDC.put("someMdc", "qwerty");
            logger.info("Some info logging");
            logger.warn("Some warn logging");
            logger.error("Some error logging");
        }
    }
}
