package dev.getelements.elements.service;

import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Used to authorize requests users using Facebook Apple Sign-in tokens.
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
public interface AppleSignInAuthService {

    /**
     * Creates a new session using the supplied identity token.
     *
     * Will validate token signature using Apple's identity certificates.
     *
     * @param applicationNameOrId the application name or id
     * @param identityToken the identity token issued by Apple's services
     *
     * @return
     */
    AppleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(String applicationNameOrId,
                                                                              String identityToken);

}
