package com.namazustudios.socialengine.rt.jackson.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.namazustudios.socialengine.rt.PayloadWriter;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertTrue;

public class MultiContentTypeJacksonPayloadWriterModuleIntegrationTest {

    @Test
    public void testModuleIsSane() {
        final Injector injector = Guice.createInjector(new MultiContentTypeJacksonPayloadWriterModule());
        final Map<String, PayloadWriter> readers = injector.getInstance(new Key<Map<String, PayloadWriter>>(){});
        assertTrue(readers.size() > 0);
    }

}
