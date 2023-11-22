package dev.getelements.elements.model.schema;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.WHOLE_WORD_ONLY;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@MetadataSpecProperty.ValidTabs
public class MetadataSpecProperty implements Serializable {

    @NotNull
    @ApiModelProperty("The unique name of the field")
    @Pattern(regexp = WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    @ApiModelProperty("The display name of the field")
    private String displayName;

    @NotNull
    @ApiModelProperty("The field type")
    private MetadataSpecPropertyType type;

    @NotNull
    @ApiModelProperty("True if the field is required.")
    private boolean required;

    @ApiModelProperty("The placeholder value when displaying in the editor.")
    private String placeholder;

    @ApiModelProperty("The default value, if left unspecified.")
    private String defaultValue;

    private List<MetadataSpecProperty> properties;

    public MetadataSpecProperty() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetadataSpecPropertyType getType() {
        return type;
    }

    public void setType(MetadataSpecPropertyType type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<MetadataSpecProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MetadataSpecProperty> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataSpecProperty that = (MetadataSpecProperty) o;
        return isRequired() == that.isRequired() && Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && getType() == that.getType() && Objects.equals(getPlaceholder(), that.getPlaceholder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getType(), isRequired(), getPlaceholder(), getDefaultValue(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetadataSpecProperty{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", fieldType=").append(type);
        sb.append(", required=").append(required);
        sb.append(", placeHolder='").append(placeholder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @Constraint(validatedBy = ValidTabs.Validator.class)
    public @interface ValidTabs {

        String message() default "Invalid tabs field.";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        class Validator implements ConstraintValidator<ValidTabs, MetadataSpecProperty> {

            @Override
            public boolean isValid(final MetadataSpecProperty value, final ConstraintValidatorContext context) {

                final var type = value.getType();
                final var properties = value.getProperties();

                if (ARRAY.equals(type) || OBJECT.equals(type)) {
                    return checkComplexProperties(value, context);
                } else if (properties != null && !properties.isEmpty()) {
                    final var msg = format("'properties' must not be null for %s type fields.", type);
                    context.buildConstraintViolationWithTemplate(msg)
                            .addPropertyNode("tabs")
                            .addConstraintViolation();
                    return false;
                }

                return true;

            }

            private boolean checkComplexProperties(
                    final MetadataSpecProperty value,
                    final ConstraintValidatorContext context) {

                final var properties = value.getProperties();

                if (properties == null) {
                    final var msg = "'tabs' must not be null for OBJECT type fields.";
                    context.buildConstraintViolationWithTemplate(msg)
                            .addPropertyNode("tabs")
                            .addConstraintViolation();
                    return false;
                }

                return true;

            }

        }

    }

}
