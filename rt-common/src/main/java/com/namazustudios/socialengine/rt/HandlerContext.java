package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.exception.HandlerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Context._waitAsync;

/**
 * Used to manage handler-type {@link Resource} instances.  These are intended to be short-lived and managed entirely
 * on the server side.  Unlike using the bare {@link ResourceContext} the methods in this interface will automatically
 * guaranteed the lifecycle of the remote {@link Resource}.
 *
 * This exists because a client may not effectively manage a remote temporary {@link Resource}.  For example, a service
 * passing {@link Request} instances to a {@link Resource} may be terminated before it can effectively destroy the
 * {@link Resource} handling the {@link Request}.  This would essentially leak the {@link Resource} in memory.
 *
 *
 */
@Proxyable
public interface HandlerContext {



    /**
     * Synchronously invokes {@link #invokeSingleUseHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)}
     * blocking until the call returns.
     *
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     * @return the invocation result
     */
    @RemotelyInvokable
    default Object invokeSingleUseHandler(
            @Serialize final Attributes attributes,
            @Serialize final String module, @Serialize final String method,
            @Serialize final Object ... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Object> objectFuture = invokeSingleUseHandlerAsync(
            o -> logger.debug("Got response: {}", o),
            th -> logger.error("Got error from remote", th),
            attributes, module, method, args);

        return _waitAsync(logger, objectFuture);

    }

    /**
     * Creates a {@link Resource}, invokes a method on that {@Link Resource}, and returns the result as either a
     * success or a failure.  The {@link HandlerContext} will assign a temporary {@link Path}, and {@link ResourceId}
     * which will not be exposed to the client code.
     *
     * Further, this will guaranteed the destruction of the created {@link Resource} even if the client abandons the
     * call this method.
     *
     * The underlying {@link HandlerContext} may time out the {@link Resource}.  In which case the caller will receive
     * an instance fo {@link HandlerTimeoutException} through the returned {@link Future} or in the
     * {@link Consumer<Throwable>} handed to the method.
     *
     * @param success invoked when the invocation successfully returns
     * @param failure invoked whent he invocation encounters an error
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     *
     * @return {@link Future<Object>} which can be used to obtain the result of the invocation.
     */
    @RemotelyInvokable
    Future<Object> invokeSingleUseHandlerAsync(
        @ResultHandler Consumer<Object> success, @ErrorHandler Consumer<Throwable> failure,
        @Serialize Attributes attributes, @Serialize String module,
        @Serialize String method, @Serialize Object ... args);

    /**
     * Synchronously invokes {@link #invokeRetainedHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)}
     * blocking until the call returns.
     *
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     * @return the invocation result
     */
    @RemotelyInvokable
    default Object invokeRetainedHandler(
            @Serialize final Attributes attributes,
            @Serialize final String module, @Serialize final String method,
            @Serialize final Object ... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Object> objectFuture = invokeSingleUseHandlerAsync(
                o -> logger.debug("Got response: {}", o),
                th -> logger.error("Got error from remote", th),
                attributes, module, method, args);

        return _waitAsync(logger, objectFuture);

    }

    /**
     * Creates a {@link Resource}, invokes a method on that {@Link Resource}, and returns the result as either a
     * success or a failure.  The {@link HandlerContext} will assign a temporary {@link Path}, and {@link ResourceId}
     * which will not be exposed to the client code.
     *
     * Unlike {@link #invokeSingleUseHandlerAsync(Consumer, Consumer, Attributes, String, String, Object...)} this will
     * just unlink the {@link Resource} after it invokes the specified method.  It will be responsiblity of the invoked
     * method to provide further retentions should it be desired.
     *
     * The underlying {@link HandlerContext} may time out the {@link Resource}.  In which case the caller will receive
     * an instance fo {@link HandlerTimeoutException} through the returned {@link Future} or in the
     * {@link Consumer<Throwable>} handed to the method.
     *
     * @param success invoked when the invocation successfully returns
     * @param failure invoked whent he invocation encounters an error
     * @param attributes the {@link Attributes} supplied to the underlying {@link Resource}
     * @param module the module name used to instantiate the resource {@see {@link ResourceContext#create(String, Path, Object...)}}
     * @param method the method to invoke {@see {@link Resource#getMethodDispatcher(String)}}
     * @param args the arguments passed to the method {@see {@link MethodDispatcher#params(Object...)}}
     * @return the {@link Future<Object>} of the result.
     */
    @RemotelyInvokable
    Future<Object> invokeRetainedHandlerAsync(
        @ResultHandler Consumer<Object> success, @ErrorHandler Consumer<Throwable> failure,
        @Serialize Attributes attributes, @Serialize String module,
        @Serialize String method, @Serialize Object ... args);

}
