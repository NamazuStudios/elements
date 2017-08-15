package com.namazustudios.socialengine.rt.manifest;

import java.util.List;
import java.util.Map;

/**
 * Contains a listing of all {@link Operation}
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpManifest {

    private List<Operation> operationList;

    private List<String> produces;

    private List<String> consumes;

    private Map<String, String> methods;

    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public Map<String, String> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, String> methods) {
        this.methods = methods;
    }

}
