package dev.getelements.elements.annotation;

import dev.getelements.elements.util.PemDecoder;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Constraint(validatedBy = PemDecoder.Validator.class)
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

}
