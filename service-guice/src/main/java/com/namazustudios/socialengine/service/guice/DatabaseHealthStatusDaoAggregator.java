package com.namazustudios.socialengine.service.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.dao.DatabaseHealthStatusDao;

import java.util.Set;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class DatabaseHealthStatusDaoAggregator extends PrivateModule {

    @Override
    protected void configure() {
        // For now there's only one database. However if we incorporate multiple sources of data (for example, mongo
        // and redis) we can aggregate the various DAOs that provide access to the database.
        final var setBinder = newSetBinder(binder(), DatabaseHealthStatusDao.class);
        setBinder.addBinding().toProvider(getProvider(DatabaseHealthStatusDao.class));
        expose(new TypeLiteral<Set<DatabaseHealthStatusDao>>(){});
    }

}
