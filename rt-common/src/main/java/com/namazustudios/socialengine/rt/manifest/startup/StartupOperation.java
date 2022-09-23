package com.namazustudios.socialengine.rt.manifest.startup;

import com.namazustudios.socialengine.rt.ParameterizedPath;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.model.Type;
import com.namazustudios.socialengine.rt.manifest.security.AuthScheme;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Represents a single operation performed by a startup function.
 *
 */
public class StartupOperation {

    @NotNull
    private String name;

    @NotNull
    private String method;

    @NotNull
    private Map<String, Type> parameters;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartupOperation)) return false;

        StartupOperation operation = (StartupOperation) o;

        if (getName() != null ? !getName().equals(operation.getName()) : operation.getName() != null) return false;
        if (getMethod() != null ? !getMethod().equals(operation.getMethod()) : operation.getMethod() != null)
            return false;
        if (getParameters() != null ? !getParameters().equals(operation.getParameters()) : operation.getParameters() != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getParameters() != null ? getParameters().hashCode() : 0);
        return result;
    }

}
