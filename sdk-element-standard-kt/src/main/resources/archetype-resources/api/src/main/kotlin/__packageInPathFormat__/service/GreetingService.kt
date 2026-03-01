package com.mystudio.mygame.service

import dev.getelements.elements.sdk.annotation.ElementServiceExport

@ElementServiceExport
interface GreetingService {

    /**
     * Attempts to fetch the current user for the session header and return an appropriate greeting
     * @return The greeting based on if a logged-in user is found
     */
    fun getGreeting(): String

}