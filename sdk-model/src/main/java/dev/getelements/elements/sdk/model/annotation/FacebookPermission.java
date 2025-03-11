package dev.getelements.elements.sdk.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies a requested permission.
 *
 * @see <a href="https://developers.facebook.com/docs/facebook-login/permissions/">Facebook Permissions</a>
 *
 * Created by patricktwohig on 6/14/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FacebookPermission {

    /**
     * The name of the permission.
     *
     * @return the name of the permission.
     */
    String value();

}
