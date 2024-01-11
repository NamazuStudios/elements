package dev.getelements.elements.annotation;

import dev.getelements.elements.rt.exception.InvalidPemException;
import dev.getelements.elements.rt.util.PemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.security.spec.KeySpec;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Constraint(validatedBy = PemFile.Validator.class)
public @interface PemFile {

    String message() default "{dev.getelements.elements.annotation.PemFile.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    @Documented
    @Retention(RUNTIME)
    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @interface List {
        PemFile[] value();
    }


    /**
     * Validates a PEM file when passed
     */
    class Validator implements ConstraintValidator<PemFile, String> {

        private static final Logger logger = LoggerFactory.getLogger(Validator.class);

        @Override
        public void initialize(final PemFile constraintAnnotation) {}

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext context) {
            try {
                final PemData<KeySpec> pemData = new PemData<>(value, dummy -> new KeySpec(){});
                logger.trace("Successfully decoded pem {} {}", pemData.getLabel(), pemData);
                return true;
            } catch (InvalidPemException e) {
                context.buildConstraintViolationWithTemplate("Invalid PEM File.");
                return false;
            }
        }

    }

}
