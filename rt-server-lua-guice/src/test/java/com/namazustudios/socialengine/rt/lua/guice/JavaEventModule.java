package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;

import static org.mockito.Mockito.mock;

public class JavaEventModule extends AbstractModule {

    private final TestJavaEvent tje = mock(TestJavaEvent.class);

    @Override
    protected void configure() {
        bind(TestJavaEvent.class).toInstance(tje);
    }
}
