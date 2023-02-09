package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.ParameterizedPath;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.security.AuthScheme;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a single operation performed over an HTTP request.  This contains a
 * verb, path, method, and metadata on how the request is consumed and the response
 * is produced.
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpOperation implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private HttpVerb verb;

    @NotNull
    private ParameterizedPath path;

    @NotNull
    private String method;

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull HttpParameter> parameters;

    @Valid
    @NotNull
    private List<@NotNull String> authSchemes;

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull HttpContent> producesContentByType;

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull HttpContent> consumesContentByType;

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
     * @return a {@link Map<String, HttpParameter>} containing the parameter metadata
     */
    public Map<String, HttpParameter> getParameters() {
        return remapParameters(parameters);
    }

    /**
     * Sets the parameters this operation accepts by type.
     *
     * @param parameters a {@link Map<String, HttpParameter>} containing the parameter metadata
     */
    public void setParameters(Map<String, HttpParameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the auth schemes supported by this {@link HttpOperation}.  The value returned corresponds to the name of the
     * {@link AuthScheme} as specified by {@link AuthScheme#getName()}.
     *
     * @return the auth schemes supported by this {@link HttpOperation}
     */
    public List<String> getAuthSchemes() {
        return authSchemes;
    }

    /**
     * Sets the {@link AuthScheme} supported by this {@link HttpOperation}.
     * @param authSchemes
     */
    public void setAuthSchemes(List<String> authSchemes) {
        this.authSchemes = authSchemes;
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

    /**
     * Sorts the {@link HttpContent} parameters based off of its index, to keep parameter order consistent
     */
    public void sortParameters() {
        parameters = remapParameters(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpOperation)) return false;

        HttpOperation operation = (HttpOperation) o;

        if (getName() != null ? !getName().equals(operation.getName()) : operation.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(operation.getDescription()) : operation.getDescription() != null)
            return false;
        if (getVerb() != operation.getVerb()) return false;
        if (getPath() != null ? !getPath().equals(operation.getPath()) : operation.getPath() != null) return false;
        if (getMethod() != null ? !getMethod().equals(operation.getMethod()) : operation.getMethod() != null)
            return false;
        if (getParameters() != null ? !getParameters().equals(operation.getParameters()) : operation.getParameters() != null)
            return false;
        if (getProducesContentByType() != null ? !getProducesContentByType().equals(operation.getProducesContentByType()) : operation.getProducesContentByType() != null)
            return false;
        return getConsumesContentByType() != null ? getConsumesContentByType().equals(operation.getConsumesContentByType()) : operation.getConsumesContentByType() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getVerb() != null ? getVerb().hashCode() : 0);
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getParameters() != null ? getParameters().hashCode() : 0);
        result = 31 * result + (getProducesContentByType() != null ? getProducesContentByType().hashCode() : 0);
        result = 31 * result + (getConsumesContentByType() != null ? getConsumesContentByType().hashCode() : 0);
        return result;
    }

    private Map<String, HttpParameter> remapParameters(Map<String, HttpParameter> unsortedParameters) {

        final var list = new ArrayList<>(unsortedParameters.entrySet());
        list.sort(Map.Entry.comparingByValue());

        final var sortedParameters = new LinkedHashMap<String, HttpParameter>();

        for (var entry : list) {
            sortedParameters.put(entry.getKey(), entry.getValue());
        }

        return sortedParameters;

    }

}
