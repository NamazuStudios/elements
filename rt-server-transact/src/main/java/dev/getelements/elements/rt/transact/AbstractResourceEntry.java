package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractResourceEntry implements ResourceEntry {

    protected final OperationalStrategy operationalStrategy;

    public AbstractResourceEntry(final OperationalStrategy operationalStrategy) {
        this.operationalStrategy = operationalStrategy;
    }

    @Override
    public boolean isOriginalContents() {
        return operationalStrategy.doIsOriginalContent(this);
    }

    @Override
    public boolean isOriginalReversePaths() {
        return operationalStrategy.doIsOriginalReversePaths(this);
    }

    @Override
    public Optional<ResourceId> findResourceId() {
        return operationalStrategy.doFindResourceId(this);
    }

    @Override
    public Set<Path> getReversePathsImmutable() {
        return operationalStrategy.doGetReversePathsImmutable(this);
    }

    @Override
    public boolean link(final Path path) {
        return operationalStrategy.doLink(this, path);
    }

    @Override
    public boolean unlink(final Path path) {
        return operationalStrategy.doUnlink(this, path);
    }

    @Override
    public ResourceContents updateResourceContents() {
        return operationalStrategy.doUpdateResourceContents(this);
    }

    @Override
    public boolean delete() {
        return operationalStrategy.doDelete(this);
    }

}
