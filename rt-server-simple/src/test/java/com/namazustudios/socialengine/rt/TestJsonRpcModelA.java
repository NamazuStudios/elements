package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;

import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;

@RemoteModel(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL))
public class TestJsonRpcModelA {

    private String string;

    private int anInt;

    private float aFloat;

    private Float anObjectFloat;

    private Integer anObjectInteger;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public Float getAnObjectFloat() {
        return anObjectFloat;
    }

    public void setAnObjectFloat(Float anObjectFloat) {
        this.anObjectFloat = anObjectFloat;
    }

    public Integer getAnObjectInteger() {
        return anObjectInteger;
    }

    public void setAnObjectInteger(Integer anObjectInteger) {
        this.anObjectInteger = anObjectInteger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestJsonRpcModelA that = (TestJsonRpcModelA) o;
        return getAnInt() == that.getAnInt() && Float.compare(that.getaFloat(), getaFloat()) == 0 && Objects.equals(getString(), that.getString()) && Objects.equals(getAnObjectFloat(), that.getAnObjectFloat()) && Objects.equals(getAnObjectInteger(), that.getAnObjectInteger());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getString(), getAnInt(), getaFloat(), getAnObjectFloat(), getAnObjectInteger());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestJsonRpcModelA{");
        sb.append("string='").append(string).append('\'');
        sb.append(", anInt=").append(anInt);
        sb.append(", aFloat=").append(aFloat);
        sb.append(", anObjectFloat=").append(anObjectFloat);
        sb.append(", anObjectInteger=").append(anObjectInteger);
        sb.append('}');
        return sb.toString();
    }

}
