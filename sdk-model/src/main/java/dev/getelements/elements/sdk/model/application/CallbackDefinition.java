package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.NO_WHITE_SPACE;

/** Defines a script method and a module to invoke. */
@Schema(description = "Defines a script method and a module to invoke.")
public class CallbackDefinition implements Serializable {

    /** Creates a new instance. */
    public CallbackDefinition() {}

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @Schema(description = "Specifies the method to invoke.")
    private String method;

    @Valid
    @NotNull
    @Schema(description = "Specifies the module to invoke.")
    private ElementServiceReference service;

    /**
     * Returns the element service reference to invoke.
     * @return the service reference
     */
    public ElementServiceReference getService() {
        return service;
    }

    /**
     * Sets the element service reference to invoke.
     * @param service the service reference
     */
    public void setService(ElementServiceReference service) {
        this.service = service;
    }

    /**
     * Returns the method name to invoke.
     * @return the method name
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method name to invoke.
     * @param method the method name
     */
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        CallbackDefinition that = (CallbackDefinition) object;
        return Objects.equals(getMethod(), that.getMethod()) && Objects.equals(getService(), that.getService());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(), getService());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallbackDefinition{");
        sb.append("method='").append(method).append('\'');
        sb.append(", service=").append(service);
        sb.append('}');
        return sb.toString();
    }

}
