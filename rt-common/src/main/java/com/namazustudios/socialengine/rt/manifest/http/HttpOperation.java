package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.ParameterizedPath;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.util.Map;

/**
 * Represents a single operation performed over an HTTP request.  This contains a
 * verb, path, method, and metadata on how the request is consumed and the response
 * is produced.
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpOperation {

    private String name;

    private String description;

    private HttpVerb verb;

    private ParameterizedPath path;

    private String method;

    private Map<String, Type> parameters;

    private Map<String, HttpContent> producesContentByType;

    private Map<String, HttpContent> consumesContentByType;

    /**
     * The name of the operation.
     *
     * @return the {@link String} representing the name of the operation
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the operation.
     *
     * @param name the {@link String} representing the name of the operation
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the operation
     *
     * @return the the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the operation
     *
     * @param description  the the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

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
     * Gets the parameters this operation accepts by type.
     *
     * @return a {@link Map<String, Type>} containing the parameter metadata
     */
    public Map<String, Type> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters this operation accepts by type.
     *
     * @param parameters a {@link Map<String, Type>} containing the parameter metadata
     */
    public void setParameters(Map<String, Type> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation produces content.
     *
     * @return a list of {@link HttpContent}
     */
    public Map<String, HttpContent> getProducesContentByType() {
        return producesContentByType;
    }

    /**
     * Sets a listing of {@link HttpContent} instances which specify how
     * the operation produces content.
     *
     * @param producesContentByType  a list of {@link HttpContent}
     */
    public void setProducesContentByType(Map<String, HttpContent> producesContentByType) {
        this.producesContentByType = producesContentByType;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation consumes content.
     *
     * @return a list of {@link HttpContent}
     */
    public Map<String, HttpContent> getConsumesContentByType() {
        return consumesContentByType;
    }

    /**
     * Gets a listing of {@link HttpContent} instances which specify how
     * the operation consumes content.
     *
     * @param consumesContentByType a list of {@link HttpContent}
     */
    public void setConsumesContentByType(Map<String, HttpContent> consumesContentByType) {
        this.consumesContentByType = consumesContentByType;
    }

    /**
     * Returns the {@link HttpContent} instance for which {@link HttpContent#isDefaultContent()} returns true
     * in the values providded by {@link #getConsumesContentByType()}
     *
     * @return the default {@link HttpContent}
     */
    public HttpContent getDefaultConsumedContent() {
        return getConsumesContentByType()
            .values()
            .stream()
            .filter(c -> c.isDefaultContent())
            .findFirst()
            .orElseThrow(() -> new InternalException("No default Content Type Found for " + getName()));
    }

    /**
     * Returns the {@link HttpContent} instance for which {@link HttpContent#isDefaultContent()} returns true
     * in the values providded by {@link #getProducesContentByType()}
     *
     * @return the default {@link HttpContent}
     */
    public HttpContent getDefaultProducedContent() {
        return getProducesContentByType()
            .values()
            .stream()
            .filter(c -> c.isDefaultContent())
            .findFirst()
            .orElseThrow(() -> new InternalException("No default Content Type Found for " + getName()));
    }

}
