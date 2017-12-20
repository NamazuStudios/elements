package com.namazustudios.socialengine.rt.jackson.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.namazustudios.socialengine.rt.PayloadReader;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertTrue;

public class MultiContentTypeJacksonPayloadReaderModuleIntegrationTest {

    @Test
    public void testModuleIsSane() {
        final Injector injector = Guice.createInjector(new MultiContentTypeJacksonPayloadReaderModule());
        final Map<String, PayloadReader> readers = injector.getInstance(new Key<Map<String, PayloadReader>>(){});
        assertTrue(readers.size() > 0);
    }

}
