package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

@Expose(@ModuleDefinition("test.java.event"))
public interface TestJavaEvent {

    void helloWorldEvent();

    void who(String who);

    void whoWithCount(String who, String count);
}
