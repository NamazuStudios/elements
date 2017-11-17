package com.namazustudios.socialengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJavaModule {

    private static final Logger logger = LoggerFactory.getLogger(TestJavaModule.class);

    public void helloWorld() {
        logger.info("Hello World!");
    }

    public String returnHelloWorld() {
        return "Hello World!";
    }

    public int testOverload(int a) {
        return a;
    }

    public int testOverload(int a, int b) {
        return a + b;
    }

}
