package com.mystudio.mygame.guice

import com.google.inject.PrivateModule
import com.mystudio.mygame.service.GreetingService
import com.mystudio.mygame.service.GreetingServiceImpl

class MyGameModule : PrivateModule() {

    override fun configure() {
        bind(GreetingService::class.java).to(GreetingServiceImpl::class.java)
        expose(GreetingService::class.java)
    }

}
