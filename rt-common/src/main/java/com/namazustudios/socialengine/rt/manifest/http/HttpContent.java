package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.manifest.model.Model;

import java.util.List;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class HttpContent {

    private String type;

    private Model model;

    private List<String> headers;

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
    public Model getModel() {
        return model;
    }

    /**
     * Sets the model definition
     *
     * @param model the model definition
     */
    public void setModel(Model model) {
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
