#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
public interface GreetingService {

    /**
     * Attempts to fetch the current user for the session header and return an appropriate greeting
     * @return The greeting based on if a logged-in user is found
     */
    String getGreeting();

}
