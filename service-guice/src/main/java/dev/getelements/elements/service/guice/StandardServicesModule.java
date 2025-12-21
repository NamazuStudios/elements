package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.sdk.service.advancement.AdvancementService;
import dev.getelements.elements.sdk.service.auth.CustomAuthSessionService;
import dev.getelements.elements.sdk.service.auth.SessionService;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.notification.NotificationService;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.service.advancement.StandardAdvancementService;
import dev.getelements.elements.service.auth.DefaultSessionService;
import dev.getelements.elements.service.auth.StandardCustomAuthSessionService;
import dev.getelements.elements.service.name.SimpleAdjectiveAnimalNameService;
import dev.getelements.elements.service.notification.StandardNotificationService;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;
import jakarta.inject.Named;

/**
 * Houses services that do not follow a majority of the scoping rules, but also do not exist strictly in the unscoped
 * space. These are services that perform simple tasks like name generation, session generation etc. which can be
 * exceptions to the security principles (it no injected user) but are visible to the code as regular services not
 * requiring the {@link Named} with the name {@link Constants#UNSCOPED} annotation.
 */
public class StandardServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(NameService.class)
                .to(SimpleAdjectiveAnimalNameService.class)
                .asEagerSingleton();

        bind(CustomAuthSessionService.class)
                .to(StandardCustomAuthSessionService.class);

        bind(AdvancementService.class)
                .to(StandardAdvancementService.class);

        bind(SessionService.class)
                .to(DefaultSessionService.class);

        bind(VersionService.class)
                .to(BuildPropertiesVersionService.class)
                .asEagerSingleton();

    }

}
