package com.namazustudios.socialengine.rt.manifest.http;

import java.util.Map;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class HttpModule {

    private String module;

    private Map<String, HttpOperation> operationsByName;

    /**
     * Gets the name fo the module.  This typically names the language-specific type or class name
     * which is used to load the underlying logic.
     *
     * @return the name of the module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the name of the module.
     *
     * @param module the name of the module
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Gets a mapping of operations by name.
     *
     * @return a map of operations by name.
     */
    public Map<String, HttpOperation> getOperationsByName() {
        return operationsByName;
    }

    /**
     * Sets the map of operations by name.
     *
     * @param operationsByName the map of operations by name
     */
    public void setOperationsByName(Map<String, HttpOperation> operationsByName) {
        this.operationsByName = operationsByName;
    }

}
