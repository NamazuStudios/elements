package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;

@Expose(modules = {
    "test.java.exceptions"
})
public class TestJavaExceptions {

    public void throwType(final Class<? extends Exception> exceptionClass) throws Exception {
        throw exceptionClass.newInstance();
    }

}
