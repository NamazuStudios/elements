package dev.getelements.elements.cluster.server

import dev.getelements.elements.sdk.annotation.ElementEventConsumer
import dev.getelements.elements.sdk.cluster.id.DeploymentId
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher.ResultAsyncHandler
import dev.getelements.elements.sdk.cluster.remote.annotation.Dispatch
import dev.getelements.elements.sdk.cluster.remote.annotation.ErrorHandler
import dev.getelements.elements.sdk.cluster.remote.annotation.ResultHandler
import dev.getelements.elements.sdk.cluster.remote.annotation.Serialize
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult
import dev.getelements.elements.sdk.cluster.remote.exception.BadParameterException
import dev.getelements.elements.sdk.cluster.remote.exception.MethodNotFoundException
import dev.getelements.elements.sdk.cluster.remote.exception.ServiceNotFoundException
import dev.getelements.elements.sdk.cluster.util.Reflection
import dev.getelements.elements.sdk.deployment.ElementRuntimeService
import dev.getelements.elements.sdk.record.ElementServiceKey
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class StandardRemoteInvocationDispatcher : RemoteInvocationDispatcher {

    private val runtimes: MutableMap<DeploymentId, ElementRuntimeService.RuntimeRecord> = ConcurrentHashMap();

    /**
     * Resolving and invoking a method can block (arbitrarily long, in the case of [Dispatch.Type.FUTURE]'s
     * [Future.get] call) so [dispatch] hands the work off here rather than running it on the caller's thread
     * (the Jetty websocket message-dispatch thread). Virtual threads are used since this workload is
     * blocking-call-bound and per-invocation, not CPU-bound - the endpoint's [Session.asyncRemote] is expected
     * to be safe to invoke from these worker threads.
     */
    private val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    override fun dispatch(invocation: Invocation, handler: ResultAsyncHandler) {
        executor.execute { doDispatch(invocation, handler) }
    }

    private fun doDispatch(invocation: Invocation, handler: ResultAsyncHandler) {

        try {

            val address = invocation.address()
            val remoteElementAddress = address.service().element()
            val elementAddress = remoteElementAddress.address()
            val serviceAddress = address.service().service()
            val methodAddress = address.method()

            val deploymentId = remoteElementAddress.instance().deploymentId()
                ?: throw ServiceNotFoundException("No deployment id specified in invocation address.")

            val runtimeRecord = runtimes[deploymentId]
                ?: throw ServiceNotFoundException("No runtime loaded for deployment: $deploymentId")

            val element = runtimeRecord.elements().firstOrNull {
                it.getElementRecord().definition().name() == elementAddress.name()
            } ?: throw ServiceNotFoundException("No element named ${elementAddress.name()} in deployment $deploymentId")

            val classLoader = element.getElementRecord().classLoader()

            val serviceClass = try {
                Class.forName(serviceAddress.type(), false, classLoader)
            } catch (ex: ClassNotFoundException) {
                throw ServiceNotFoundException("Service type not found: ${serviceAddress.type()}", ex)
            }

            @Suppress("UNCHECKED_CAST")
            val serviceKey = ElementServiceKey<Any>(serviceClass as Class<Any>, serviceAddress.name())

            val serviceInstance = element.getServiceLocator()
                .findInstance(serviceKey)
                .orElseThrow { ServiceNotFoundException("No service found for key: $serviceKey") }
                .get()

            val parameterClasses = try {
                invocation.parameters().map { Class.forName(it, false, classLoader) }.toTypedArray()
            } catch (ex: ClassNotFoundException) {
                throw BadParameterException("Unable to resolve parameter type.", ex)
            }

            val method = try {
                serviceClass.getMethod(methodAddress.name(), *parameterClasses)
            } catch (ex: NoSuchMethodException) {
                throw MethodNotFoundException(
                    Reflection.noSuchMethod(serviceClass, methodAddress.name(), parameterClasses).message,
                    ex
                )
            }

            val args = buildArguments(method, invocation, handler)

            val returnValue = try {
                method.invoke(serviceInstance, *args)
            } catch (ex: InvocationTargetException) {
                reportError(handler, ex.targetException ?: ex)
                return
            }

            when (invocation.dispatchType()) {

                Dispatch.Type.SYNCHRONOUS ->
                    handler.onInvocationResult(InvocationResult(returnValue, InvocationResult.RETURN_VALUE))

                Dispatch.Type.FUTURE -> if (returnValue is Future<*>) {
                    try {
                        val futureValue = returnValue.get()
                        handler.onInvocationResult(InvocationResult(futureValue, InvocationResult.RETURN_VALUE))
                    } catch (ex: ExecutionException) {
                        reportError(handler, ex.cause ?: ex)
                    }
                } else {
                    handler.onInvocationResult(InvocationResult(returnValue, InvocationResult.RETURN_VALUE))
                }

                Dispatch.Type.ASYNCHRONOUS, Dispatch.Type.SUBSCRIPTION -> {
                    // Results/errors for these dispatch types already flow through the synthesized
                    // @ResultHandler/@ErrorHandler proxies built in buildArguments().
                }

                else -> Unit

            }

        } catch (ex: Exception) {
            reportError(handler, ex)
        }

    }

    private fun buildArguments(method: Method, invocation: Invocation, handler: ResultAsyncHandler): Array<Any?> {

        val parameters = method.parameters
        val argumentsIterator = invocation.arguments().iterator()

        return Array(parameters.size) { index ->
            val parameter = parameters[index]
            when {

                parameter.getAnnotation(Serialize::class.java) != null ->
                    if (argumentsIterator.hasNext()) argumentsIterator.next() else null

                parameter.getAnnotation(ResultHandler::class.java) != null ->
                    newResultHandlerProxy(parameter, index, handler)

                parameter.getAnnotation(ErrorHandler::class.java) != null ->
                    newErrorHandlerProxy(parameter, handler)

                else -> Reflection.getDefaultValue(parameter)

            }
        }

    }

    private fun newResultHandlerProxy(
        parameter: Parameter,
        param: Int,
        handler: ResultAsyncHandler
    ): Any {

        val type = parameter.type
        val handlerMethod = Reflection.getHandlerMethod(parameter)

        return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, methodArgs ->
            if (method == handlerMethod) {
                val value = methodArgs?.getOrNull(0)
                handler.onInvocationResult(InvocationResult(value, param))
            }
            null
        }

    }

    private fun newErrorHandlerProxy(
        parameter: Parameter,
        handler: ResultAsyncHandler
    ): Any {

        val type = parameter.type
        val handlerMethod = Reflection.getHandlerMethod(parameter)

        return Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, methodArgs ->
            if (method == handlerMethod) {
                val throwable = methodArgs?.getOrNull(0) as? Throwable ?: RuntimeException("Unknown remote error")
                handler.onInvocationError(InvocationError(throwable))
            }
            null
        }

    }

    private fun reportError(handler: ResultAsyncHandler, throwable: Throwable) {
        handler.onInvocationError(InvocationError(throwable))
    }

    @ElementEventConsumer(ElementRuntimeService.RUNTIME_LOADED)
    fun onRuntimeLoaded(runtimeRecord: ElementRuntimeService.RuntimeRecord) {
        val deploymentId = DeploymentId.forUniqueName(runtimeRecord.deployment.id);
        runtimes[deploymentId] = runtimeRecord
    }

    @ElementEventConsumer(ElementRuntimeService.RUNTIME_UNLOADED)
    fun onRuntimeUnloaded(deploymentIdString: String) {
        val deploymentId = DeploymentId.forUniqueName(deploymentIdString);
        runtimes.remove(deploymentId)
    }

}
