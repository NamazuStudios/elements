package dev.getelements.elements.sdk.test.element.mongo;

import dev.getelements.elements.sdk.Element;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bound eagerly by {@link TestRedeployGuiceModule}, so its constructor runs at Element injector-build time --
 * inside the runtime service's load phase, before the Element is mounted. This mirrors the real-world
 * element-side {@code MorphiaIndexInitializer} from issue #209: Element code that touches the platform's
 * shared Morphia {@link Datastore} for the Element's own entity while the Element's injector is being built.
 *
 * <p>On a hot reload this is exactly where the cross-loader collision surfaces. The shared Mapper caches
 * entity models by FQCN; a find() here decodes any stored row through whichever {@code EntityModel} the
 * Mapper currently holds for {@code TestRedeployDocument}:
 *
 * <ul>
 *     <li>Pre-fix (#209), on the second generation's load the Mapper still holds the <em>first</em>
 *     generation's model (the old Element's deregistration was deferred to the container service's
 *     event-driven unmount, which runs after this constructor). Decoding yields first-generation instances
 *     which cannot be cast to this generation's class -- {@code ClassCastException: X cannot be cast to X} --
 *     and the reload fails.</li>
 *     <li>Post-fix, the previous generation's entities were deregistered and the Mapper rebuilt before this
 *     Element's load, so the model is mapped fresh from this generation's class and the find/save succeed.</li>
 * </ul>
 */
public class TestRedeployIndexInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TestRedeployIndexInitializer.class);

    /**
     * Flipped when this generation's close hooks fire. Static on purpose: each Element generation has its
     * OWN classloader and therefore its own copy of this class, so the flag is per-generation. The
     * hot-reload test reads the OLD generation's copy (via the generation's still-loaded class) to assert
     * that the platform invoked the Element's close hooks on the hot-reload unload path -- the exact
     * mechanism Element authors rely on to stop background executors (#209, Fix C).
     */
    public static final AtomicBoolean ON_CLOSE_FIRED = new AtomicBoolean(false);

    @Inject
    public TestRedeployIndexInitializer(final Datastore datastore, final Element element) {

        // Register the same lifecycle hook the Fix C recipe documents for Element authors: a close hook
        // that must fire when this Element generation is closed -- including the hot-reload unload path.
        // A real Element stops its background executors here; this test Element records that the hook ran.
        //
        // The hook MUST be registered via constructor injection of the Element, NOT via the static
        // ElementSupplier/ElementRegistrySupplier: their backing statics are set only AFTER the injector
        // finishes building (GuiceElementLoader binds them post-createInjector), so an eager singleton's
        // constructor observes null from the static suppliers. Injection works at any point in the
        // lifecycle, including here at injector-build time.
        element.onClose(el -> {
            logger.info("Element closing; this generation's onClose hook firing");
            ON_CLOSE_FIRED.set(true);
        });

        // Decode any rows left by a previous generation through the Mapper's currently-cached model. Pre-fix
        // this is where the cross-loader ClassCastException materializes: the cached model belongs to the
        // previous generation's classloader, so decoding yields instances of the old class.
        final var existing = datastore.find(TestRedeployDocument.class).iterator();
        try (existing) {
            while (existing.hasNext()) {
                logger.info("Found existing {} id={}",
                        TestRedeployDocument.class.getName(), existing.next().getId());
            }
        }

        // Leave a row behind so the next generation's find() has something to decode.
        final var document = new TestRedeployDocument();
        document.setId("hot-reload-probe-" + System.nanoTime());
        document.setText("Written by " + TestRedeployIndexInitializer.class.getName());
        datastore.save(document);

        logger.info("{} initialized; entity model resolved and probe row saved",
                TestRedeployIndexInitializer.class.getName());

    }

}
