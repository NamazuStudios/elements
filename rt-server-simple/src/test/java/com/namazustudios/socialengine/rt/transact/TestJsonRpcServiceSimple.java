package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.transact.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;

@RemoteService(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_HTTP_PROTOCOL))
public class TestJsonRpcServiceSimple {

    @RemotelyInvokable
    public void testVoidNoParams() {}

    @RemotelyInvokable
    public void testVoidStringParam(String foo) {}

    @RemotelyInvokable
    public void testVoidByteParam(byte i) {}

    @RemotelyInvokable
    public void testVoidCharParam(char i) {}

    @RemotelyInvokable
    public void testVoidShortParam(short i) {}

    @RemotelyInvokable
    public void testVoidIntParam(int i) {}

    @RemotelyInvokable
    public void testVoidLongParam(long i) {}

    @RemotelyInvokable
    public void testVoidFloatParam(float f) {}

    @RemotelyInvokable
    public void testVoidDoubleParam(double d) {}

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
