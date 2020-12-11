package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;

@Expose(modules = {
        "test.java.event"
})
public interface TestJavaEvent {

    void helloWorldEvent();
}
