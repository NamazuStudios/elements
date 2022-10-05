package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.Serialize;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.transact.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;

@RemoteService(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_HTTP_PROTOCOL))
public class TestJsonRpcServiceSimple {

    @RemotelyInvokable
    public void testVoidNoParams() {}

    @RemotelyInvokable
    public void testVoidStringParam(String foo) {}

    @RemotelyInvokable
    public void testVoidByteParam(@Serialize byte i) {}

    @RemotelyInvokable
    public void testVoidCharParam(@Serialize char i) {}

    @RemotelyInvokable
    public void testVoidShortParam(@Serialize short i) {}

    @RemotelyInvokable
    public void testVoidIntParam(@Serialize int i) {}

    @RemotelyInvokable
    public void testVoidLongParam(@Serialize long i) {}

    @RemotelyInvokable
    public void testVoidFloatParam(@Serialize float f) {}

    @RemotelyInvokable
    public void testVoidDoubleParam(@Serialize double d) {}

    @RemotelyInvokable
    public String testReturnString() {
        return "";
    }

    @RemotelyInvokable
    public byte testReturnByte() {
        return 0;
    }

    @RemotelyInvokable
    public char testReturnChar() {
        return 0;
    }

    @RemotelyInvokable
    public short testReturnShort() {
        return 0;
    }

    @RemotelyInvokable
    public int testReturnInt() {
        return 0;
    }

    @RemotelyInvokable
    public long testReturnLong() {
        return 0;
    }

    @RemotelyInvokable
    public float testReturnFloat() {
        return 0;
    }

    @RemotelyInvokable
    public float testReturnDouble() {
        return 0;
    }

}
