package dev.getelements.elements.rt.testkit;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.getelements.elements.rt.testkit.UnimplementedMock.unimplemented;

public class UnitTestModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(UnitTestModule.class);

    private Set<Class<?>> types = new HashSet<>();

    private List<Runnable> bindings = new ArrayList<>();

    @Override
    protected void configure() {
        bindings.forEach(runnable -> runnable.run());
    }

    public <T> void mock(final Class<T> type) {
        // TODO Effectively, mocking is disabled for now.  We will have to address this in a better way later.
        if (!type.isEnum() && types.add(type)) {
            final T mock = unimplemented(type);
            logger.info("Adding unimplemented mock for {}", type.getName());
            bindings.add(() -> bind(type).toInstance(mock));
        }
    }

    public <T> void mock(ModuleDefinition def, final Class<T> type) {
        // TODO Effectively, mocking is disabled for now.  We will have to address this in a better way later.
        if (!type.isEnum() && types.add(type)) {
            final T mock = unimplemented(type);
            logger.info("Adding unimplemented mock for {}", type.getName());
            bindings.add(() -> bind(type).toInstance(mock));
        }
    }

}
