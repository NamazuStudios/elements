package dev.getelements.elements.model.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toSet;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidProperties.Validator.class)
public @interface ValidProperties {

    String message() default "Invalid properties field.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidProperties, MetadataSpecPropertiesContainer> {

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
