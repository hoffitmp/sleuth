package com.example.demo;

public enum MDCKEY {
    // local baggage --> not propagated downstream, and may change multiple times within nested spans
    /**
     * all keys have to be defined in application.properties (!!!!!!!)
     * spring.sleuth.(baggage-keys|local-keys|propagation-keys) keys that also should appear in
     * logging MDC also have to be listed in application.properties (!!!!!!!!!)
     * spring.sleuth.log.slf4j.whitelist-mdc-keys They have to also to appear EXACTLY the same in
     * the logging.pattern of application.properties
     */

    INSTANCE("i"), OPERATION("op"), CHUNK("chunk");

    private String mdcKey;

    MDCKEY(String tag) {
        this.mdcKey = tag;
    }

    @Override
    public String toString() {
        return mdcKey;
    }
}

