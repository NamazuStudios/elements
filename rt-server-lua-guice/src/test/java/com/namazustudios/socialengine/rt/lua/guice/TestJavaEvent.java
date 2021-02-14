package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

@Expose(@ExposedModuleDefinition("test.java.event"))
public interface TestJavaEvent {

    void helloWorldEvent();

    void who(String who);

    void whoWithCount(String who, String count);
}
