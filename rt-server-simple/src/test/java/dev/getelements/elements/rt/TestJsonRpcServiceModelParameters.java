package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.RemoteScope;
import dev.getelements.elements.rt.annotation.RemoteService;
import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.annotation.Serialize;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static dev.getelements.elements.rt.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;

@RemoteService(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL))
public class TestJsonRpcServiceModelParameters {

    @RemotelyInvokable
    public void testVoidAcceptModelA(@Serialize("modelA") final TestJsonRpcModelA modelA) {}

    @RemotelyInvokable
    public void testVoidAcceptModelB(@Serialize("modelB") final TestJsonRpcModelB modelB) {}

    @RemotelyInvokable
    public TestJsonRpcModelA testReturnModelA() {
        return new TestJsonRpcModelA();
    }

    @RemotelyInvokable
    public TestJsonRpcModelB testReturnModelB() {
        return new TestJsonRpcModelB();
    }

}
