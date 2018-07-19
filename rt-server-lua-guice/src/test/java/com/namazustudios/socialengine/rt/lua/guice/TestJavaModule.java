package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Expose(module = "test.java.module")
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
