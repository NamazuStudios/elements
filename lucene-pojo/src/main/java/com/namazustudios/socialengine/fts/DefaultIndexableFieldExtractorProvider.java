package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/15/15.
 */
public class DefaultIndexableFieldExtractorProvider implements IndexableFieldExtractor.Provider {

    private static final DefaultIndexableFieldExtractorProvider INSTANCE = new DefaultIndexableFieldExtractorProvider();

    private DefaultIndexableFieldExtractorProvider() {}

    public static DefaultIndexableFieldExtractorProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> IndexableFieldExtractor<T> get(FieldMetadata searchableField) {
        try {
            return searchableField.extractor().newInstance();
        } catch (IllegalAccessException ex) {
            throw new DocumentException(ex);
        } catch (InstantiationException ex) {
            throw new DocumentException(ex);
        }
    }

}
