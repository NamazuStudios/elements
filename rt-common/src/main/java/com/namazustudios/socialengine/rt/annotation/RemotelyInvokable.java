package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Designates a method as being remotely invokable.  The method must be marked in a {@link Proxyable} type.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemotelyInvokable {
    RoutingStrategy[] value();

    enum RoutingStrategy {
        /**
         * The invocation should be called on all application nodes in the network. Note that this is an
         * "all-or-nothing" call for now, i.e. if any single application node invocation fails, then this entire
         * procedure will fail.
         *
         * If the invocation originated on an app node, that same app node will also receive the invocation but without
         * entering the network layer.
         */
        AGGREGATE,

        /**
         * The invocation is addressed to a specific resource on a specific application node as defined within a
         * ResourceId.
         *
         * If the invocation originated on an app node and matches the local app node UUID, then the invocation will
         * occur without entering the network layer.
         */
        ADDRESSED,

        /**
         * The invocation should occur on the least-resource-constrained app node (this may be defined as the app node
         * that, at the time of the local invocation, had reported the smallest load avg or the smallest in-memory
         * resources), e.g. a single use invocation. Necessarily, an ANY invocation is neither addressed, nor should it
         * be run more than exactly once on exactly one app node in the network.
         *
         * If the invocation originated on an app node, that same app node will be considered as a viable candidate for
         * performing the invocation. The resource utilization poll of the local app node should not occur over the
         * network and neither should the local invocation (should the local app node be chosen to perform it).
         */
        ANY;

        public static RoutingStrategy[] determine(final Method method) {
            final RemotelyInvokable remotelyInvokable = method.getAnnotation(RemotelyInvokable.class);
            return remotelyInvokable.value();
        }
    }
}
