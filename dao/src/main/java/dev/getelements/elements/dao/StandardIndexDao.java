package dev.getelements.elements.dao;

import javax.inject.Inject;
import java.util.Set;

public class StandardIndexDao implements IndexDao {

    private Set<HasIndexableMetadata> indexableMetadataSet;

    @Override
    public void buildAllIndexes() {
        getIndexableMetadataSet().forEach(HasIndexableMetadata::buildIndexes);
    }

    public Set<HasIndexableMetadata> getIndexableMetadataSet() {
        return indexableMetadataSet;
    }

    @Inject
    public void setIndexableMetadataSet(Set<HasIndexableMetadata> indexableMetadataSet) {
        this.indexableMetadataSet = indexableMetadataSet;
    }

}
