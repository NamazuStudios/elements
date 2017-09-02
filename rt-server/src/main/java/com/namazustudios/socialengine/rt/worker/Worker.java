//package com.namazustudios.socialengine.rt.worker;
//
//import com.namazustudios.socialengine.rt.Container;
//import com.namazustudios.socialengine.rt.LockService;
//import com.namazustudios.socialengine.rt.Request;
//import com.namazustudios.socialengine.rt.Resource;
//import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
//
///**
// * A Worker is a type of {@link Resource} which is intented to live for a period of time in the
// * {@link Container <Worker>} instance until it is destroyed by a process (including itself).
// *
// * Typically {@link Worker}s are designed for long-running logic which may yield its execution for
// * some time.  The instance of {@link Worker} accepts {@link Request} instances through its
// * {@link WorkerRequestHandler} instance.
// *
// * As a {@link Worker} may exist for a long period of time, it may receive requests and may be accessed
// * by multiple clients at a time.  It is the job of the {@link Container <Worker>} to ensure access
// * is locked appropriately (typically using a {@link LockService} instance.
// *
// * Created by patricktwohig on 8/23/15.
// */
//public interface Worker extends Resource {
//
//    /**
//     * Gets the RequestHandler for the method.
//     *
//     * @param method the method name
//     * @return the handler for the given method, never null
//     *
//     * @throws {@link MethodNotFoundException} if the method cannot be found.
//     */
//    WorkerRequestHandler getHandler(final String method);
//
//}
