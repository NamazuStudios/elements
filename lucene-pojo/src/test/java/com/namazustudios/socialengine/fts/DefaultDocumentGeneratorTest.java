package com.namazustudios.socialengine.fts;

import org.testng.annotations.BeforeTest;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class DefaultDocumentGeneratorTest extends AbstractDocumentGeneratorTest {

    @Override
    public DocumentGenerator getObjectToTest() {
        return new DefaultDocumentGenerator();
    }

}
