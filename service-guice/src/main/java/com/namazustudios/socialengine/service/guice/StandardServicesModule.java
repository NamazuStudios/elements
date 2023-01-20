package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.advancement.StandardAdvancementService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.service.auth.StandardCustomAuthSessionService;
import com.namazustudios.socialengine.service.name.SimpleAdjectiveAnimalNameService;

/**
 * Houses services that do not follow a majority of the scoping rules, but also do not exist strictly in the unscoped
 * space. These are services that perform simple tasks like name generation, session generation etc. which can be
 * exceptions to the security principles (it no injected user) but are visible to the code as regular services not
 * requiring the {@link Unscoped} annotation.
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
