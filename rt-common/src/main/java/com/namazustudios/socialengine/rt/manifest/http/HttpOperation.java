package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.ParameterizedPath;

import java.util.List;

/**
 * Represents a single operation performed over an HTTP request.  This contains a
 * verb, path, method, and metadata on how the request is consumed and the response
 * is produced.
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpOperation {

    private HttpVerb verb;

    private ParameterizedPath path;

    private String method;

    private List<HttpContent> produces;

    private List<HttpContent> consumes;

    /**
     * Gets the associated {@link HttpVerb} associated with the request.
     *
     * @return the verb
     */
    public HttpVerb getVerb() {
        return verb;
    }

    /**
     * Sets the associated {@link HttpVerb} associated with the request.
     *
     * @param verb
     */
    public void setVerb(HttpVerb verb) {
        this.verb = verb;
    }

    /**
     * Gets the path matching the request, this is unparsed and reflects the
     * raw settings.
     *
     * @return the path
     */
    public ParameterizedPath getPath() {
        return path;
    }

    /**
     * Sets the path of the request.
     *
     * @param path the path
     */
    public void setPath(ParameterizedPath path) {
        this.path = path;
    }

    /**
     * Gets the underlying method to call when servicing the request.
     *
     * @return the method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the underlying method to call when servicing the request.
     *
     * @param method the method name.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation produces content.
     *
     * @return a list of {@link HttpContent}
     */
    public List<HttpContent> getProduces() {
        return produces;
    }

    /**
     * Sets a listing of {@link HttpContent} instances which specify how
     * the operation produces content.
     *
     * @param produces  a list of {@link HttpContent}
     */
    public void setProduces(List<HttpContent> produces) {
        this.produces = produces;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation consumes content.
     *
     * @return a list of {@link HttpContent}
     */
    public List<HttpContent> getConsumes() {
        return consumes;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation consumes content.
     *
     * @param consumes a list of {@link HttpContent}
     */
    public void setConsumes(List<HttpContent> consumes) {
        this.consumes = consumes;
    }

}
