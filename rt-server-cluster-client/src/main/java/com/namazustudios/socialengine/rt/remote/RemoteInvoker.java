package com.namazustudios.socialengine.rt.remote;

import java.util.concurrent.Future;

public interface RemoteInvoker {

    Future<Object> invoke(final Invocation invocation);

}
