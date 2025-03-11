package dev.getelements.elements.guice;

import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletModule.FilterKeyBindingBuilder;
import com.google.inject.servlet.ServletModule.ServletKeyBindingBuilder;

/**
 * A utility interface which allows the for the separation of logic from the {@link ServletModule}.
 */
public interface ServletBindings {

    void useBasicAuthFor(String urlPattern);

    void useGlobalAuthFor(String restApiRoot);

    void useStandardAuthFor(String urlPattern);

    FilterKeyBindingBuilder filter(String urlPattern, String... morePatterns);

    FilterKeyBindingBuilder filter(Iterable<String> urlPatterns);

    FilterKeyBindingBuilder filterRegex(String regex, String... regexes);

    FilterKeyBindingBuilder filterRegex(Iterable<String> regexes);

    ServletKeyBindingBuilder serve(String urlPattern, String... morePatterns);

    ServletKeyBindingBuilder serve(Iterable<String> urlPatterns);

    ServletKeyBindingBuilder serveRegex(String regex, String... regexes);

    ServletKeyBindingBuilder serveRegex(Iterable<String> regexes);
}
