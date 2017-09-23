package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.EventListener;

public class GuiceMain extends GuiceServletContextListener {

    private final Injector containerInjector;

    private Injector injector;

    private ServletContext servletContext;

    private final TypeListener typeListener = new TypeListener() {
        @Override
        public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
            if (servletContext != null && isListener(type)) {
                encounter.register((MembersInjector<? super I>) instance -> servletContext.addListener((EventListener) instance));
            }
        }
    };

    private <I> boolean isListener(final TypeLiteral<I> typeLiteral) {
        final Class<?> cls = typeLiteral.getRawType();
        return cls.isAnnotationPresent(WebListener.class) && EventListener.class.isAssignableFrom(cls);
    }

    public GuiceMain(final Injector containerInjector) {
        this.containerInjector = containerInjector;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        injector = null;
    }

    @Override
    protected Injector getInjector() {
        return injector = containerInjector.createChildInjector(
            new DispatcherModule(),
            new DispatcherServletMappings(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bindListener(Matchers.any(), typeListener);
                }
            });
    }

}
