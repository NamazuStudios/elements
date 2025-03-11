package dev.getelements.elements.rt.jersey;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

public class CsvFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(CsvMessageBodyWriter.class);
        return true;
    }

}
