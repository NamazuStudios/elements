package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.transact.SimpleJsonRpcManifestTestHappy.HAPPY_SCOPE;

@RemoteModel(scopes = @RemoteScope(scope = HAPPY_SCOPE, protocol = ELEMENTS_JSON_RPC_HTTP_PROTOCOL))
public class TestJsonRpcModelB {

    private TestJsonRpcModelA anEmbeddedObj;

    private Map<String, String> aGenericMap;

    private List<String> aGenericList;

    private List<TestJsonRpcModelA> anObjectList;

    public TestJsonRpcModelA getAnEmbeddedObj() {
        return anEmbeddedObj;
    }

    public void setAnEmbeddedObj(TestJsonRpcModelA anEmbeddedObj) {
        this.anEmbeddedObj = anEmbeddedObj;
    }

    public Map<String, String> getaGenericMap() {
        return aGenericMap;
    }

    public void setaGenericMap(Map<String, String> aGenericMap) {
        this.aGenericMap = aGenericMap;
    }

    public List<String> getaGenericList() {
        return aGenericList;
    }

    public void setaGenericList(List<String> aGenericList) {
        this.aGenericList = aGenericList;
    }

    public List<TestJsonRpcModelA> getAnObjectList() {
        return anObjectList;
    }

    public void setAnObjectList(List<TestJsonRpcModelA> anObjectList) {
        this.anObjectList = anObjectList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestJsonRpcModelB that = (TestJsonRpcModelB) o;
        return Objects.equals(getAnEmbeddedObj(), that.getAnEmbeddedObj()) && Objects.equals(getaGenericMap(), that.getaGenericMap()) && Objects.equals(getaGenericList(), that.getaGenericList()) && Objects.equals(getAnObjectList(), that.getAnObjectList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAnEmbeddedObj(), getaGenericMap(), getaGenericList(), getAnObjectList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestJsonRpcModelB{");
        sb.append("anEmbeddedObj=").append(anEmbeddedObj);
        sb.append(", aGenericMap=").append(aGenericMap);
        sb.append(", aGenericList=").append(aGenericList);
        sb.append(", anObjectList=").append(anObjectList);
        sb.append('}');
        return sb.toString();
    }

}
