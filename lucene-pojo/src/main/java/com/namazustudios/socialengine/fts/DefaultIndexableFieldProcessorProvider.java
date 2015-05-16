package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/15/15.
 */
public final class DefaultIndexableFieldProcessorProvider implements IndexableFieldProcessor.Provider {

    private static final DefaultIndexableFieldProcessorProvider INSTANCE = new DefaultIndexableFieldProcessorProvider();

    private DefaultIndexableFieldProcessorProvider() {}

    public static DefaultIndexableFieldProcessorProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> IndexableFieldProcessor<T> get(FieldMetadata fieldMetadata,
                                              Class<? extends IndexableFieldProcessor> implementationClass) {
        try {
            return implementationClass.newInstance();
        } catch (IllegalAccessException ex) {
            throw new DocumentException(ex);
        } catch (InstantiationException ex) {
            throw new DocumentException(ex);
        }
    }

}
