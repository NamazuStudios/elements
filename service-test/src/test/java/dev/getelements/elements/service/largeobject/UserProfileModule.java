package dev.getelements.elements.service.largeobject;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;

import java.util.Optional;

import static org.mockito.Mockito.spy;

public class UserProfileModule extends AbstractModule {

    @Override
    protected void configure() {

        final var userSpy = spy(User.class);
        userSpy.setId("permittedUserId");
        bind(User.class).toInstance(userSpy);

        final var profileSpy = spy(Profile.class);
        profileSpy.setId("permittedProfileId");
        bind(new TypeLiteral<Optional<Profile>>(){}).toInstance(Optional.of(profileSpy));

    }

}
