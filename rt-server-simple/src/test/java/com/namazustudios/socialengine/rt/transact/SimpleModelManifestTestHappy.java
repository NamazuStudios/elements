package com.namazustudios.socialengine.rt.transact;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.ModelManifestService;
import com.namazustudios.socialengine.rt.SimpleModelManifestService;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.jackson.JacksonModelIntrospector;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.namazustudios.socialengine.rt.SimpleModelManifestService.RPC_MODELS;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = {SimpleModelManifestTestHappy.Module.class, ValidationModule.class})
public class SimpleModelManifestTestHappy {

    public static final String HAPPY_SCOPE = SimpleJsonRpcManifestTestHappy.HAPPY_SCOPE;

    private ModelManifestService underTest;

    private ModelIntrospector modelIntrospector;

    @DataProvider
    public static Object[][] getModelsToTest() {
        return new Object[][] {
                {TestJsonRpcModelA.class},
                {TestJsonRpcModelB.class}
        };
    }

    @Test(dataProvider = "getModelsToTest")
    public void testModel(final Class<?> cls) {
        final var name = RemoteModel.Util.getName(cls);
        final var scope = RemoteModel.Util.getScope(cls, ELEMENTS_JSON_RPC_HTTP_PROTOCOL, HAPPY_SCOPE);
        final var models = getUnderTest().getModelManifest().getModelsByName();
        final var actual = models.get(name);
        final var expected = getModelIntrospector().introspectClassForModel(cls, scope);
        assertEquals(actual, expected);
    }

    public ModelManifestService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(ModelManifestService underTest) {
        this.underTest = underTest;
    }

    public ModelIntrospector getModelIntrospector() {
        return modelIntrospector;
    }

    @Inject
    public void setModelIntrospector(ModelIntrospector modelIntrospector) {
        this.modelIntrospector = modelIntrospector;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final var services = newSetBinder(
                    binder(),
                    new TypeLiteral<Class<?>>(){},
                    Names.named(RPC_MODELS)
            );

            bind(Mapper.class).to(DozerBeanMapper.class);
            bind(ModelIntrospector.class).to(JacksonModelIntrospector.class);
            bind(ModelManifestService.class).to(SimpleModelManifestService.class);

            services.addBinding().toInstance(TestJsonRpcModelA.class);
            services.addBinding().toInstance(TestJsonRpcModelB.class);

            bind(String.class).annotatedWith(Names.named(REMOTE_SCOPE)).toInstance(HAPPY_SCOPE);
            bind(String.class).annotatedWith(Names.named(REMOTE_PROTOCOL)).toInstance(ELEMENTS_JSON_RPC_HTTP_PROTOCOL);

        }

    }

}
