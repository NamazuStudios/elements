package dev.getelements.elements.rt;

import javax.inject.Inject;
import javax.inject.Provider;

public class RequestAttributesProvider implements Provider<Attributes> {

    private Request request;

    @Override
    public Attributes get() {
        return getRequest().getAttributes();
    }

    public Request getRequest() {
        return request;
    }

    @Inject
    public void setRequest(Request request) {
        this.request = request;
    }

}
