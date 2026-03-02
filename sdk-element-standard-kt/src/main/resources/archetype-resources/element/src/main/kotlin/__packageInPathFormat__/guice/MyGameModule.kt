package ${package}.guice

import com.google.inject.PrivateModule
import ${package}.service.GreetingService
import ${package}.service.GreetingServiceImpl

class MyGameModule : PrivateModule() {

    override fun configure() {
        bind(GreetingService::class.java).to(GreetingServiceImpl::class.java)
        expose(GreetingService::class.java)
    }

}
