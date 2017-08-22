package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

/**
 * Cooperates with the {@link AssetLoader} to load manifest instances such as {@link HttpManifest}.  This is used
 * by the container to describe how to load modules and execute code in response to network events (such as HTTP
 * requests).
 *
 * Additionally, the manifest service provide any other metadata to describe or document the body of code that
 * the service is hosting.
 *
 * As a general rule, there should be a one to one relationship between instances of {@link AssetLoader} and
 * ManifestLoader.  Sharing or pooling loaders can be failitated by using {@link AssetLoader#getReferenceCountedView()}
 * to ensure resources are pooled appropriately.
 *
 * Created by patricktwohig on 8/14/17.
 */
public interface ManifestLoader extends AutoCloseable {

    /**
     * Gets the {@link ModelManifest} instance.
     *
     * @return the {@link ModelManifest}
     */
    ModelManifest getModelManifest();

    /**
     * Gets the manifest for the HTTP mappings, if available.
     *
     * @return the
     */
    HttpManifest getHttpManifest();

    /**
     * Closes the {@link ManifestLoader} as well as any open resources associated therein.  This also closes
     * the underlying {@link AssetLoader} which may be associated with this {@link ManifestLoader}.  If
     * pooling or sharing resources of {@link AssetLoader} instances is desired, then something such as
     * {@link AssetLoader#getReferenceCountedView()} could be used.
     */
    @Override
    void close();

}
