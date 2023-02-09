package com.namazustudios.socialengine.rt;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestTestModule.HAPPY_SCOPE;
import static org.testng.Assert.assertEquals;

@Guice(modules = {SimpleModelManifestTestHappy.Module.class, ValidationModule.class})
public class SimpleModelManifestTestHappy {

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
        final var scope = RemoteModel.Util.getScope(cls, ELEMENTS_JSON_RPC_PROTOCOL, HAPPY_SCOPE);
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

    public static class Module extends SimpleJsonRpcManifestTestModule {
        @Override
        protected void configureTypes() {
            bindModel(TestJsonRpcModelA.class);
            bindModel(TestJsonRpcModelB.class);
        }
    }

}
