package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.propagation.ExtraFieldPropagation;

@Profile("receiver")
@RestController
public class Receiver {
    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private Tracer tracer;

    @GetMapping("/receive")
    public String receive() {
        log.info(String.format("START /receive %s", ""));

        String businessDomain = ExtraFieldPropagation.get(tracer.currentSpan().context(), BAGGAGEKEY.BUSINESS_DOMAIN.toString());
        String businessProcess = ExtraFieldPropagation.get(tracer.currentSpan().context(), BAGGAGEKEY.BUSINESS_PROCESS_NAME.toString());
        String pids = ExtraFieldPropagation.get(tracer.currentSpan().context(), BAGGAGEKEY.BUSINESS_PROCESS_IDS.toString());
        String operation = ExtraFieldPropagation.get(tracer.currentSpan().context(), MDCKEY.OPERATION.toString());
        String instance = ExtraFieldPropagation.get(tracer.currentSpan().context(), MDCKEY.INSTANCE.toString());
        String chunk = ExtraFieldPropagation.get(tracer.currentSpan().context(), MDCKEY.CHUNK.toString());

        log.info(String.format("gotten: %s/%s/%s/%s/%s/%s", businessDomain, businessProcess, pids, operation, instance, chunk));

        // wrong place to do so, as these will be copied into MDC not before innerSpan.start()
        ExtraFieldPropagation.set(tracer.currentSpan().context(), MDCKEY.OPERATION.toString(), "rootOperation");
        ExtraFieldPropagation.set(tracer.currentSpan().context(), MDCKEY.INSTANCE.toString(), "i0");
        ExtraFieldPropagation.set(tracer.currentSpan().context(), MDCKEY.CHUNK.toString(), "receiveChunk"); // never to be seen

        log.info("set local ExtraFieldPropagation on automatic span"); // all three wrongs above not there in log

        Span innerSpan = tracer.nextSpan().name("innerSpan");
        ExtraFieldPropagation.set(innerSpan.context(), MDCKEY.CHUNK.toString(), "innerChunk");

        log.info("before innerSpan start"); // still not in MDC as innerSpan not started yet
        try (SpanInScope innerSpanInScope = tracer.withSpanInScope(innerSpan.start())) {
            log.info("innerSpan started");

            log.info("innerSpan ending soon");
        } catch (Throwable t) {
            // already parent's Span in scope again, as SpanInScope already auto.close()d
            log.error("inner", t);
            innerSpan.annotate("Exception in innerSpan: " + t.getMessage());
            throw t;
        } finally {
            // already parent's Span in scope again, as SpanInScope already auto.close()d
            log.info("innerSpan finishing...");
            innerSpan.finish();
            log.info("innerSpan finished");
            MDC.remove(BAGGAGEKEY.BUSINESS_PROCESS_IDS.toString());
            MDC.remove(BAGGAGEKEY.BUSINESS_DOMAIN.toString());
            MDC.remove(BAGGAGEKEY.BUSINESS_PROCESS_NAME.toString());
        }

        log.info("after innerSpan finished");


        log.info(String.format("END /receive %s", ""));
        return "received";
    }

}