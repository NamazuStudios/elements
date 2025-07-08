package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;

import java.net.URL;
import java.util.List;

public record ElementsLocalFactoryRecord(
        Attributes attributes,
        List<URL> classpath,
        List<ElementsLocalApplicationElementRecord> elements) {}
