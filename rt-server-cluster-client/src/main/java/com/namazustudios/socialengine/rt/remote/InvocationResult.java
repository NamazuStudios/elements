package com.namazustudios.socialengine.rt.remote;

public class InvocationResult {

    private Object result;

    private Throwable throwable;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

}
