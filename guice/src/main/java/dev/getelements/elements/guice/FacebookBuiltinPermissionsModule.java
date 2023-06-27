package dev.getelements.elements.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.annotation.FacebookPermission;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/15/17.
 */
public class FacebookBuiltinPermissionsModule extends AbstractModule {

    private final Supplier<List<FacebookPermission>> facebookPermissionListSupplier;

    public FacebookBuiltinPermissionsModule(Supplier<List<FacebookPermission>> facebookPermissionListSupplier) {
        this.facebookPermissionListSupplier = facebookPermissionListSupplier;
    }

    @Override
    protected void configure() {
        binder().bind(new TypeLiteral<Supplier<List<FacebookPermission>>>(){})
                .toInstance(facebookPermissionListSupplier);
    }

}
