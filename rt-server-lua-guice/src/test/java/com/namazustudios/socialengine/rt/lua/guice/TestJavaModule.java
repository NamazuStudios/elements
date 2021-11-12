package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.lua.guice.rest.SimpleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Expose(modules = {
    "test.java.module.a",
    "test.java.module.b",
})
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

    public int testAcceptModel(SimpleModel model) {
        return 0;
    }

    public void throwException() {
        throw new IllegalArgumentException("exception");
    }

    public void throwException(final String message) {
        throw new IllegalArgumentException(message);
    }

}
