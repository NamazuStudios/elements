package com.namazustudios.socialengine.rt.manifest.http;

import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class HttpContent {

    private String type;

    private String model;

    private List<String> headers;

    private Map<String, String> staticHeaders;

    private boolean defaultContent;

    /**
     * Gets the type type.
     *
     * @return the type type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the desired "Content-Type"
     *
     * @param type the type type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the model definition.
     *
     * @return the model defintion
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model definition
     *
     * @param model the model definition
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets any additional http headers used by this operation.
     *
     * @return a {@link List <String>} indicating the headers
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Sets any additional http headers used by this operation.
     *
     * @param headers  a {@link List<String>} indicating the headers
     */
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    /**
     * Returns true if the content type is the default content type.  If only
     * one is specified, then this will always return the default content type.
     *
     * @return true  if this is the default http content.
     */
    public boolean isDefaultContent() {
        return defaultContent;
    }

    /**
     * Set to true if the content type is the default content type.  If only
     * one is specified, then this will always return the default content type.
     *
     * @@param defaultContent true  if this is the default http content.
     */
    public void setDefaultContent(boolean defaultContent) {
        this.defaultContent = defaultContent;
    }

    /**
     * Returns static headers which are associated with this {@link HttpContent}.  This can be
     * used to ensure that certain specific headers are automatically inserted in every response,
     * or must be present in every request.
     *
     * @return a {@link Map<String, String>} of static header values
     */
    public Map<String, String> getStaticHeaders() {
        return staticHeaders;
    }

    /**
     * Specifies static headers which are associated with this {@link HttpContent}.  This can be
     * used to ensure that certain specific headers are automatically inserted in every response,
     * or must be present in every request.
     *
     * @return a {@link Map<String, String>} of static header values
     */
    public void setStaticHeaders(Map<String, String> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

}
