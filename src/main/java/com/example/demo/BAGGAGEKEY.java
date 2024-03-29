package com.example.demo;

public enum BAGGAGEKEY {

    /**
     * all keys have to be defined in application.properties (!!!!!!!)
     * spring.sleuth.(baggage-keys|local-keys|propagation-keys) keys that also should appear in
     * logging MDC also have to be listed in application.properties (!!!!!!!!!)
     * spring.sleuth.log.slf4j.whitelist-mdc-keys They have to also to appear EXACTLY the same in
     * the logging.pattern of application.properties
     */
    /* static baggage = stay the same throughout the whole trace (multi-process business flow) */
    BUSINESS_PROCESS_IDS("bpids"), BUSINESS_DOMAIN("ddd"), BUSINESS_PROCESS_NAME("bp")

    /* dynamic baggage = may change throughout the whole trace (multi-process business flow) */
    // none
    ;

    private String key;

    BAGGAGEKEY(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
