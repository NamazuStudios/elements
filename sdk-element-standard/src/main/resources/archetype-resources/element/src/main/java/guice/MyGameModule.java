#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.guice;

import com.google.inject.PrivateModule;
import ${package}.service.GreetingService;
import ${package}.service.GreetingServiceImpl;

public class MyGameModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(GreetingService.class).to(GreetingServiceImpl.class);

        expose(GreetingService.class);
    }
}