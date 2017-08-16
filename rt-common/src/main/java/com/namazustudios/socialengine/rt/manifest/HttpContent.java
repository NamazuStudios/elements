package com.namazustudios.socialengine.rt.manifest;

import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class HttpContent {

    private String content;

    private Map<String, Object> model;

    private List<String> headers;

    /**
     * Gets the content type.
     *
     * @return the content type.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the desired "Content-Type"
     *
     * @param content the content type
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the model definition.
     *
     * @return the model defintion
     */
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * Sets the model definition
     *
     * @param model the model definition
     */
    public void setModel(Map<String, Object> model) {
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

}
