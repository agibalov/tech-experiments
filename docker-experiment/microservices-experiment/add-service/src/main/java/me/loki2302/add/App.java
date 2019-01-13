package me.loki2302.add;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RestController
    public static class AddController {
        @RequestMapping(value = "/add/{a}/{b}", method = RequestMethod.GET)
        public ResponseDTO addNumbers(
                @PathVariable("a") int a,
                @PathVariable("b") int b) {
            
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.a = a;
            responseDTO.b = b;
            responseDTO.result = a + b;
            return responseDTO;
        }
    }

    public static class ResponseDTO {
        public int a;
        public int b;
        public int result;
    }
}
