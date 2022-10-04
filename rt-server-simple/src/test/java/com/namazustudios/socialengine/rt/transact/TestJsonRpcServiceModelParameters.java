package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.transact.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;

@RemoteService(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_HTTP_PROTOCOL))
public class TestJsonRpcServiceModelParameters {

    @RemotelyInvokable
    public void testVoidAcceptModelA(final TestJsonRpcModelA modelA) {}

    @RemotelyInvokable
    public void testVoidAcceptModelB(final TestJsonRpcModelB modelB) {}

    @RemotelyInvokable
    public TestJsonRpcModelA testReturnModelA() {
        return new TestJsonRpcModelA();
    }

    @RemotelyInvokable
    public TestJsonRpcModelB testReturnModelB() {
        return new TestJsonRpcModelB();
    }

}
