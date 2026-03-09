package dev.getelements.elements.sdk.model.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toSet;

/**
 * Validates that the {@code properties} field of a {@link MetadataSpecPropertiesContainer} is consistent
 * with the declared type (e.g., ARRAY requires exactly one property, OBJECT requires a non-null list).
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidProperties.Validator.class)
public @interface ValidProperties {

    /**
     * Returns the constraint violation message.
     *
     * @return the message
     */
    String message() default "Invalid properties field.";

    /**
     * Returns the validation groups this constraint belongs to.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Returns the payload associated with this constraint.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /** The constraint validator implementation for {@link ValidProperties}. */
    class Validator implements ConstraintValidator<ValidProperties, MetadataSpecPropertiesContainer> {

        /** Creates a new instance. */
        public Validator() {}

        private static final Logger logger = LoggerFactory.getLogger(ValidProperties.class);

        @Override
        public boolean isValid(final MetadataSpecPropertiesContainer value, final ConstraintValidatorContext context) {

            final var type = value.getType();

            if (ARRAY.equals(type)) {
                return checkArrayProperties(value, context);
            } else if (OBJECT.equals(type)) {
                return checkObjectProperties(value, context);
            }

            return true;

        }

        private boolean checkArrayProperties(
                final MetadataSpecPropertiesContainer value,
                final ConstraintValidatorContext context) {

            final var properties = value.getProperties();

            if (properties == null || properties.size() != 1) {
                final var msg = "'properties' must have exactly one element for ARRAY type fields.";
                context.buildConstraintViolationWithTemplate(msg)
                        .addPropertyNode("tabs")
                        .addConstraintViolation();
                return false;
            }

            return true;

        }

        private boolean checkObjectProperties(
                final MetadataSpecPropertiesContainer value,
                final ConstraintValidatorContext context) {

            final var properties = value.getProperties();

            if (properties == null) {
                final var msg = "'properties' must not be null for OBJECT type fields.";
                context.buildConstraintViolationWithTemplate(msg)
                        .addPropertyNode("tabs")
                        .addConstraintViolation();
                return false;
            }

            final var names = properties.stream()
                    .map(MetadataSpecProperty::getName)
                    .collect(toSet());

            final var duplicates = names.stream()
                    .filter(name -> frequency(names, name) > 1)
                    .collect(toSet());

            duplicates.forEach(duplicate -> {
                final var msg = format("'properties' contains duplicate property '%s.'", duplicate);
                context.buildConstraintViolationWithTemplate(msg)
                        .addPropertyNode("tabs")
                        .addConstraintViolation();
            });

            return duplicates.isEmpty();

        }

    }

}
