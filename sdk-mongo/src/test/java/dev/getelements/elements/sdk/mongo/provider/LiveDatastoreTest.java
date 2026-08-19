package dev.getelements.elements.sdk.mongo.provider;

import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

public class LiveDatastoreTest {

    @Test
    public void forwardsToTheCurrentlyLiveDatastore() {

        final var first = mock(Datastore.class);
        final var firstMapper = mock(Mapper.class);
        when(first.getMapper()).thenReturn(firstMapper);

        final var ref = new AtomicReference<>(first);
        final var proxy = LiveDatastore.wrap(ref);

        assertSame(proxy.getMapper(), firstMapper);

    }

    @Test
    public void aReferenceCapturedBeforeARebuildStillReflectsTheLiveDatastoreAfterwards() {

        final var first = mock(Datastore.class);
        final var firstMapper = mock(Mapper.class);
        when(first.getMapper()).thenReturn(firstMapper);

        final var ref = new AtomicReference<>(first);

        // Simulates an eager singleton capturing the injected Datastore once, before any
        // Element register/unregister rebuild has happened.
        final var capturedBeforeRebuild = LiveDatastore.wrap(ref);
        assertSame(capturedBeforeRebuild.getMapper(), firstMapper);

        // Simulates MongoElementEntityRegistrar.rebuildDatastore() swapping in a brand-new
        // Datastore/Mapper after an Element registers or unregisters entity classes.
        final var second = mock(Datastore.class);
        final var secondMapper = mock(Mapper.class);
        when(second.getMapper()).thenReturn(secondMapper);
        ref.set(second);

        // The reference captured before the rebuild must see the new Datastore -- not the one
        // that was live when it was captured.
        assertSame(capturedBeforeRebuild.getMapper(), secondMapper);

    }

    @Test
    public void neverInvokesTheDelegateAtWrapTime() {

        final var datastore = mock(Datastore.class);
        final var ref = new AtomicReference<>(datastore);

        LiveDatastore.wrap(ref);

        verifyNoInteractions(datastore);

    }

}
