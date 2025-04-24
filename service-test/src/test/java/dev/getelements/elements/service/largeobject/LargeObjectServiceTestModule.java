package dev.getelements.elements.service.largeobject;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.validation.Validator;
import jakarta.ws.rs.client.Client;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.CDN_OUTSIDE_URL;
import static org.mockito.Mockito.mock;

public class LargeObjectServiceTestModule extends AbstractModule {

    @Override
    protected void configure() {

        //real one
        bind(AccessRequestUtils.class).toInstance(new AccessRequestUtils());

        //mock
        bind(ValidationHelper.class).toInstance(mock(ValidationHelper.class));
        bind(LargeObjectDao.class).toInstance(mock(LargeObjectDao.class));
        bind(LargeObjectBucket.class).toInstance(mock(LargeObjectBucket.class));
        bind(ApplicationDao.class).toInstance(mock(ApplicationDao.class));
        bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
        bind(ProfileService.class).toInstance(mock(ProfileService.class));
        bind(UserDao.class).toInstance(mock(UserDao.class));
        bind(NameService.class).toInstance(mock(NameService.class));
        bind(UserService.class).toInstance(mock(UserService.class));
        bind(Validator.class).toInstance(mock(Validator.class));
        bind(Attributes.class).toInstance(mock(Attributes.class));
        bind(Client.class).toInstance(mock(Client.class));

        bind(String.class).annotatedWith(named(CDN_OUTSIDE_URL)).toInstance("https://cdn.example.com");

    }
}
