//package com.namazustudios.socialengine.rt.worker;
//
//import com.namazustudios.socialengine.rt.Request;
//import com.namazustudios.socialengine.rt.ResponseReceiver;
//import com.namazustudios.socialengine.rt.exception.BadRequestException;
//
///**
// * Used by {@link Worker} instances to handle {@link Request}s.
// *
// * Created by patricktwohig on 8/23/15.
// */
//public interface WorkerRequestHandler {
//
//    /**
//     * Gets the {@link Class} which can be passed as the payload
//     * to this object.
//     *
//     * @return the type
//     */
//    Class<?> getPayloadType();
//
//    /**
//     * Handles the given request from a edgeClient.
//     *at is not compatible
//     * with this instance and exception can be raised.  Acceptability can be determined by
//     * the usage of {@link Class#isAssignableFrom(Class)}.
//     *
//     * @param reque
//     * In the event the {@link Request#getPayload()} method returns an object thst the {@link Request} object
//     * @param responseReceiver the request object
//     *
//     * @throws {@link BadRequestException} if the return of the {@link Request#getPayload()} method is not suitable.
//     *
//     */
//    void handle(Request request, ResponseReceiver responseReceiver);
//
//}
