package dev.getelements.elements.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.WHOLE_WORD_ONLY;
import static dev.getelements.elements.model.schema.template.TemplateFieldType.ARRAY;
import static dev.getelements.elements.model.schema.template.TemplateFieldType.OBJECT;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@TemplateTabField.ValidTabs
public class TemplateTabField implements Serializable {

    @NotNull
    @ApiModelProperty("The unique name of the field")
    @Pattern(regexp = WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    @ApiModelProperty("The display name of the field")
    private String displayName;

    @NotNull
    @ApiModelProperty("The field type")
    private TemplateFieldType fieldType;

    @NotNull
    @ApiModelProperty("True if the field is required.")
    private Boolean isRequired;

    @ApiModelProperty("The placeholder value when displaying in the editor.")
    private String placeHolder;

    @ApiModelProperty("The default value, if left unspecified.")
    private String defaultValue;

    @Valid
    @ApiModelProperty("The list of tabs (only applicable if this is of OBJECT type).")
    private List<TemplateTab> tabs;

    public TemplateTabField() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(TemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setRequired(Boolean required) {
        isRequired = required;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateTabField that = (TemplateTabField) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && getFieldType() == that.getFieldType() && Objects.equals(getIsRequired(), that.getIsRequired()) && Objects.equals(getPlaceHolder(), that.getPlaceHolder()) && Objects.equals(getDefaultValue(), that.getDefaultValue()) && Objects.equals(getTabs(), that.getTabs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getFieldType(), getIsRequired(), getPlaceHolder(), getDefaultValue(), getTabs());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemplateTabField{");
        sb.append("name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", fieldType=").append(fieldType);
        sb.append(", isRequired=").append(isRequired);
        sb.append(", placeHolder='").append(placeHolder).append('\'');
        sb.append(", defaultValue='").append(defaultValue).append('\'');
        sb.append(", tabs=").append(tabs);
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

        class Validator implements ConstraintValidator<ValidTabs, TemplateTabField> {
            @Override
            public boolean isValid(final TemplateTabField value, final ConstraintValidatorContext context) {

                final var tabs = value.getTabs();
                final var type = value.getFieldType();

                if (ARRAY.equals(type)) {
                    return checkArrayTabs(value, context);
                } else if (OBJECT.equals(type)) {
                    return checkObjectTabs(value, context);
                } else if (tabs != null && !tabs.isEmpty()) {
                    final var msg = format("'tabs' must not be null for %s type fields.", type);
                    context.buildConstraintViolationWithTemplate(msg)
                            .addPropertyNode("tabs")
                            .addConstraintViolation();
                    return false;
                }

                return true;

            }

            private boolean checkObjectTabs(final TemplateTabField value, final ConstraintValidatorContext context) {

                final var tabs = value.getTabs();

                if (tabs == null) {
                    final var msg = "'tabs' must not be null for OBJECT type fields.";
                    context.buildConstraintViolationWithTemplate(msg)
                            .addPropertyNode("tabs")
                            .addConstraintViolation();
                    return false;
                }

                return true;

            }

            private boolean checkArrayTabs(final TemplateTabField value, final ConstraintValidatorContext context) {

                final var tabs = value.getTabs();

                if (tabs == null || tabs.size() != 1) {
                    final var msg = "'tabs' must specify exactly one tab for ARRAY type fields.";
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
