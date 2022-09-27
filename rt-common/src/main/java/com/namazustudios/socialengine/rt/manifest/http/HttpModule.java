package com.namazustudios.socialengine.rt.manifest.http;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class HttpModule implements Serializable {

    @NotNull
    private String module;

    @NotNull
    private Map<@NotNull String, @NotNull HttpOperation> operationsByName;

    /**
     * Gets the name of the module.  This typically names the language-specific type or class name
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
        for (Map.Entry<String,HttpOperation> entry : operationsByName.entrySet())
            entry.getValue().sortParameters();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpModule)) return false;

        HttpModule that = (HttpModule) o;

        if (getModule() != null ? !getModule().equals(that.getModule()) : that.getModule() != null) return false;
        return getOperationsByName() != null ? getOperationsByName().equals(that.getOperationsByName()) : that.getOperationsByName() == null;
    }

    @Override
    public int hashCode() {
        int result = getModule() != null ? getModule().hashCode() : 0;
        result = 31 * result + (getOperationsByName() != null ? getOperationsByName().hashCode() : 0);
        return result;
    }

}
