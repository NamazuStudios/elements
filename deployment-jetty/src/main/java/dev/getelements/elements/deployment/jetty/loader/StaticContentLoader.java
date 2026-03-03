package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.record.ElementPathRecord;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Loads static content from an {@link ElementPathRecord}
 */
public abstract class StaticContentLoader implements Loader {

    private final Function<ElementPathRecord, Path> resolve;

    public StaticContentLoader(final Function<ElementPathRecord, Path> resolve) {
        this.resolve = resolve;
    }

    @Override
    public void load(final PendingDeployment pending,
                     final ElementRuntimeService.RuntimeRecord record,
                     final Element element) {

        final var elementPathRecord = record
                .elementPathsByElement()
                .get(element);

        if (elementPathRecord == null)
            return;

        final var contentRoot = resolve.apply(elementPathRecord);

        if (contentRoot == null)
            return;

        // TODO Implement this

    }

    @Override
    public void unload(Element element) {
        // TODO Implement this.
    }

    /**
     * Loads the standard static content found at {@link dev.getelements.elements.sdk.ElementPathLoader#UI_DIR}
     */
    public static class UI extends StaticContentLoader {

        public UI() {
            super(ElementPathRecord::uiContentRoot);
        }

    }

    /**
     * Loads the standard static content found at {@link dev.getelements.elements.sdk.ElementPathLoader#STATIC_DIR}
     */
    public static class Standard extends StaticContentLoader {

        public Standard() {
            super(ElementPathRecord::staticContentRoot);
        }

    }

}
