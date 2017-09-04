//package com.namazustudios.socialengine.rt.handler;
//
//import com.namazustudios.socialengine.rt.Container;
//import com.namazustudios.socialengine.rt.Request;
//import com.namazustudios.socialengine.rt.Resource;
//import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
//
///**
// *
// * An instance of {@link Resource} exists to talk to the outside world.  That is, it is responsible for handling
// * {@link Request} instances from {@link Session} instances.  This is a {@link Resource} and may be
// * represented by a script or similar.
// *
// * {@link Handler} instances are provided by the server and will be accessed by many requests.  The {@link Handler}
// * must be prepared to acccept access via multiple threads at once and must not maintain any stateful information,
// * except what may be provided by the {@link Session}.
// *
// * The owning {@link Container} will not serialize access to the {@link Handler}, but may use techniques such as
// * {@link ThreadLocal} or caching or multiple instantiations of the same {@link Handler}.
// *
// */
//public interface Handler extends Resource {
//
//    /**
//     * Gets the RequestHandler for the method.
//     *
//     * @param method the method name
//     * @return the handler for the given method, never null
//     *
//     * @throws {@link MethodNotFoundException} if the method cannot be found.
//     */
//    ClientRequestHandler getHandler(final String method);
//
//}
