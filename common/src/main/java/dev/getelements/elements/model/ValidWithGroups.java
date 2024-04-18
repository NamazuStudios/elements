package dev.getelements.elements.model;

import javax.inject.Inject;
import javax.validation.*;
import javax.validation.groups.Default;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.joining;

/**
 * Annotates a field that will validate the field using the validation groups specified in this annotations
 * {@link #groups()} property.
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE })
@Constraint(validatedBy = ValidWithGroups.Validator.class)
public @interface ValidWithGroups {

    /**
     * Get message generated when the annotation fails.
     *
     * @return the message
     */
    String message() default "{dev.getelements.elements.model.ValidForGroups.message}";

    /**
     * The groups which activate this specific validator.
     *
     * @return the groups
     */
    Class<?>[] groups() default { };

    /**
     * The value of groups to use when validating. This will override the current group and use the groups specified
     * in this value instead.
     *
     * @return the groups against which to validate
     */
    Class<?>[] value() default {};

    /**
     * The {@link Payload}, see for more details.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default { };

    class Validator implements ConstraintValidator<ValidWithGroups, Object> {

        private ValidWithGroups constraintAnnotation;

        private Class<?>[] groups;

        private javax.validation.Validator validator;

        @Override
        public void initialize(final ValidWithGroups constraintAnnotation) {

            final var groups = constraintAnnotation.value();
            final var combined = new Class<?>[groups.length + 1];
            combined[0] = Default.class;
            arraycopy(groups, 0, combined, 1, groups.length);

            this.groups = combined;
            this.constraintAnnotation = constraintAnnotation;

        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {

            if (value == null) {
                return true;
            }

            final var violations = validator.validate(value, groups);

            if (violations.isEmpty()) {
                return true;
            }

            final var message = format("Failed Validation for Groups [%s]", Stream.of(groups)
                    .filter(c -> !Default.class.equals(c))
                    .map(Class::getSimpleName)
                    .collect(joining(","))
            );

            final var builder = context.buildConstraintViolationWithTemplate(message);

            violations.stream()
                    .map(ConstraintViolation::getPropertyPath)
                    .map(path -> {

                        var result = path.toString();

                        for (var node : path) {
                            result = node.getName();
                        }

                        return result;
                    })
                    .forEach(path -> builder.addPropertyNode(path).addConstraintViolation());

            builder.addConstraintViolation();
            return false;

        }

        public javax.validation.Validator getValidator() {
            return validator;
        }

        @Inject
        public void setValidator(javax.validation.Validator validator) {
            this.validator = validator;
        }

    }
}
