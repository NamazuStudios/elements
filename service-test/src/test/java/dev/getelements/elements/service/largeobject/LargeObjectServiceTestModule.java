package dev.getelements.elements.service.largeobject;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.dao.*;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.security.ProfileOptionalSupplier;
import dev.getelements.elements.security.ProfileSupplierProvider;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import dev.getelements.elements.service.profile.UserProfileService;
import dev.getelements.elements.util.ValidationHelper;

import javax.validation.Validator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.Constants.CDN_OUTSIDE_URL;
import static org.mockito.Mockito.mock;

public class LargeObjectServiceTestModule extends AbstractModule {

    @Override
    protected void configure() {

        //real one
        bind(LargeObjectAccessUtils.class).toInstance(new LargeObjectAccessUtils());

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
        bind(Context.Factory.class).toInstance(mock(Context.Factory.class));
        bind(Attributes.class).toInstance(mock(Attributes.class));

        bind(String.class).annotatedWith(named(CDN_OUTSIDE_URL)).toInstance("https://cdn.example.com");

        bind(new TypeLiteral<Set<ProfileIdentificationMethod>>() {}).toInstance(new HashSet<>());
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);
        bind(new TypeLiteral<Optional<Profile>>(){}).toProvider(ProfileOptionalSupplier.class);

    }
}
