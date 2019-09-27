package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.propagation.ExtraFieldPropagation;

@Profile("sender")
@RestController
public class Sender {
    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    @Value("${server.port}")
    public Integer port;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    @GetMapping("/send")
    public String send() throws Exception {
        log.info(String.format("START /send %s", ""));

        Span rootSpan = tracer.newTrace().name("sender");
        ExtraFieldPropagation.set(rootSpan.context(), BAGGAGEKEY.BUSINESS_DOMAIN.toString(), "BusinessDomain");
        ExtraFieldPropagation.set(rootSpan.context(), BAGGAGEKEY.BUSINESS_PROCESS_NAME.toString(), "BusinessProcess");
        ExtraFieldPropagation.set(rootSpan.context(), BAGGAGEKEY.BUSINESS_PROCESS_IDS.toString(), "42");
        ExtraFieldPropagation.set(rootSpan.context(), MDCKEY.OPERATION.toString(), "rootOperation");
        ExtraFieldPropagation.set(rootSpan.context(), MDCKEY.INSTANCE.toString(), "i0");
        ExtraFieldPropagation.set(rootSpan.context(), MDCKEY.CHUNK.toString(), "rootChunk");

        ResponseEntity<String> response = null;

        log.info("before rootSpan start");
        try (SpanInScope spanInScope = tracer.withSpanInScope(rootSpan.start())) {
            log.info("rootSpan started");

            Span innerSpan = tracer.nextSpan().name("innerSpan");
            ExtraFieldPropagation.set(innerSpan.context(), MDCKEY.CHUNK.toString(), "innerChunk");

            log.info("before innerSpan start");
            try (SpanInScope innerSpanInScope = tracer.withSpanInScope(innerSpan.start())) {
                log.info("innerSpan started");
                
                String fooResourceUrl = "http://localhost:"+(port+1);
                response = restTemplate.getForEntity(fooResourceUrl + "/receive", String.class);
                
                log.info("innerSpan ending soon");
            } catch(Throwable t) {
                // already rootSpan in scope again, as SpanInScope already auto.close()d
                log.error("inner", t);
                innerSpan.annotate("Exception in innerSpan: " + t.getMessage());
                throw t;
            } finally {
                // already rootSpan in scope again, as SpanInScope already auto.close()d
                log.info("innerSpan finishing...");
                innerSpan.finish();
                log.info("innerSpan finished");
            }

            log.info("after innerSpan finished");

        } catch(Throwable t) {
                log.error("root", t);
                rootSpan.annotate("Exception in rootSpan: " + t.getMessage());
                throw t;
        } finally {
            log.info("rootSpan finishing...");
            rootSpan.finish();
            log.info("rootSpan finished");
        }

        log.info(String.format("END /send %s", ""));
        return String.format("sent (HttpCode: %s)", (response != null ? response.getStatusCode() : "null"));
    }
    
}