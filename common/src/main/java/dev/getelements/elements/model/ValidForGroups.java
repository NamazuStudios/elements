package dev.getelements.elements.model;

import dev.getelements.elements.annotation.PemFile;

import javax.inject.Inject;
import javax.validation.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Constraint(validatedBy = ValidForGroups.Validator.class)
public @interface ValidForGroups {

    String message() default "{dev.getelements.elements.annotation.PemFile.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    class Validator implements ConstraintValidator<ValidForGroups, Object> {

        private ValidForGroups constraintAnnotation;

        private javax.validation.Validator validator;

        @Override
        public void initialize(final ValidForGroups constraintAnnotation) {
            this.constraintAnnotation = constraintAnnotation;
        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
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
