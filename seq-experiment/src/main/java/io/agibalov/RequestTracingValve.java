package io.agibalov;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.entries;

@Slf4j
public class RequestTracingValve extends ValveBase {
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        MDC.clear();
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("ip", request.getRemoteAddr());

        long startTime = System.currentTimeMillis();
        try {
            getNext().invoke(request, response);
        } finally {
            long elapsedTime = System.currentTimeMillis() - startTime;

            HashMap<String, Object> attributes = new HashMap<>();
            attributes.put("time", elapsedTime);
            attributes.put("response", response.getStatus());

            log.info("REQUEST {}", entries(attributes));

            MDC.clear();
        }
    }
}
