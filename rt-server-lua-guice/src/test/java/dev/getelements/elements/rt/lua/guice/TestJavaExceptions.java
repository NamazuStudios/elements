package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.annotation.Expose;

@Expose(modules = {
    "test.java.exceptions"
})
public class TestJavaExceptions {

    public void throwType(final Class<? extends Exception> exceptionClass) throws Exception {
        throw exceptionClass.newInstance();
    }

}
