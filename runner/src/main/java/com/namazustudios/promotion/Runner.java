package com.namazustudios.promotion;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import java.io.File;

/**
 * A class that loads up an embedded jetty server to run the application.
 *
 * This is really only useful for debugging.
 *
 */
public class Runner
{

    public static void main( String[] args ) throws Exception
    {
        System.out.println(new File(".").getAbsolutePath());

        final Server server = new Server(8080);
        final HandlerList handlerList = new HandlerList();

        addProjectContext(handlerList, "rest-api");

        server.setHandler(handlerList);
        server.start();
        server.join();

    }

    private static void addProjectContext(final HandlerList handlerList, final String module) {

        final WebAppContext webAppContext = new WebAppContext();

        final File base = new File(module);
        final File warDir = new File(base, "src/main/webapp");

        webAppContext.setContextPath("/" + module);
        webAppContext.setResourceBase(warDir.getAbsolutePath());

        webAppContext.setConfigurations(new Configuration[] {
                new AnnotationConfiguration(),
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration(),
                new PlusConfiguration(),
                new JettyWebXmlConfiguration()
        });

        handlerList.addHandler(webAppContext);

    }
}
