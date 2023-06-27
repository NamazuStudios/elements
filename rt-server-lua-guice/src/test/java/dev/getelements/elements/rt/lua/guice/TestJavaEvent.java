package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose(@ModuleDefinition("test.java.event"))
public interface TestJavaEvent {

    void helloWorldEvent();

    void who(String who);

    void whoWithCount(String who, String count);
}
