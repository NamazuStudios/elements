package dev.getelements.elements.service;

import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Used to authorize requests users using Facebook Google Sign-in tokens.
 *
 * Created by patricktwohig on 6/22/17.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.applesignin"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.applesignin",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.applesignin",
                deprecated = @DeprecationDefinition("Use eci.elements.service.applesignin instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.applesignin",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.applesignin instead.")
        )
})
public interface GoogleSignInAuthService {

    /**
     * Creates a new session using the supplied application, application configuration, and the identity token.
     *
     * In addition to manipulating the user account, this should also convert the short-term facebook token
     * to a long-term token and supply the result.
     *
     * @param applicationNameOrId the application name or id
     * @param applicationConfigurationNameOrId the application configuration name or id
     * @param identityToken the identity token issued by Google's services
     * @param authorizationCode the authorization code issued by Google's services
     *
     * @return
     */
    GoogleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(String applicationNameOrId,
                                                                              String applicationConfigurationNameOrId,
                                                                              String identityToken,
                                                                              String authorizationCode);

}
