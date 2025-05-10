package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.Element;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.JAVA_CLASS_NAME;
import static dev.getelements.elements.sdk.model.Constants.Regexp.NO_WHITE_SPACE;

/**
 * References service to load from within an {@link Element}.
 */
@Schema
public class ElementServiceReference {

    @NotNull
    @Schema(description = "The name of the Element to reference.")
    private String elementName;

    @Pattern(regexp = JAVA_CLASS_NAME)
    @Schema(description = "Specifies the type of the service within the Element. May be null.")
    private String serviceType;

    @Pattern(regexp = NO_WHITE_SPACE)
    @Schema(description = "Specifies the name of the service within the Element. May be null.")
    private String serviceName;

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ElementServiceReference that = (ElementServiceReference) object;
        return Objects.equals(getElementName(), that.getElementName()) && Objects.equals(getServiceType(), that.getServiceType()) && Objects.equals(getServiceName(), that.getServiceName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElementName(), getServiceType(), getServiceName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ElementServiceReference{");
        sb.append("elementName='").append(elementName).append('\'');
        sb.append(", serviceType='").append(serviceType).append('\'');
        sb.append(", serviceName='").append(serviceName).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
