//package com.namazustudios.socialengine.rt;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
///**
// *
// * Essentially, this checks for two conditions.  First, it ensures that only
// * a single response is sent to the client.  In the event the request does
// * not generate a response, a null response is generated with an instance of
// * {@link ResponseCode#OK}.
// *
// * This uses an instance of {@link AtomicBoolean} to ensure that the response
// * is generated only once.
// *
// */
//public class DelegatingCheckedResponseReceiver<ResponseT extends Response> implements Consumer<Response>, AutoCloseable {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DelegatingCheckedResponseReceiver.class);
//
//    private final Request request;
//
//    private final Consumer<ResponseT> delegate;
//
//    private final Supplier<ResponseT> defaultResponseSupplier;
//
//    private final AtomicBoolean received = new AtomicBoolean();
//
//    public DelegatingCheckedResponseReceiver(final Request request,
//                                             final Consumer<ResponseT> delegate,
//                                             final Supplier<ResponseT> defaultResponseSupplier) {
//        this.request = request;
//        this.delegate = delegate;
//        this.defaultResponseSupplier = defaultResponseSupplier;
//    }
//
//    @Override
//    public void accept(ResponseT response) {
//        if (received.compareAndSet(false, true)) {
//            delegate.accept(response);
//        } else {
//            LOG.error("Attempted to dispatch duplicate responses for request {}", request);
//        }
//
//    }
//
//    @Override
//    public void close()  {
//        if (received.compareAndSet(false, true)) {
//            final ResponseT responseT = defaultResponseSupplier.get();
//            delegate.accept(responseT);
//        }
//    }
//
//}
