package com.namazustudios.socialengine.rt.transact;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.JsonRpcManifestService;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.namazustudios.socialengine.rt.annotation.CodeStyle.JVM_NATIVE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.*;

@Guice(modules={SimpleJsonRpcManifestTestHappy.Module.class, ValidationModule.class})
public class SimpleJsonRpcManifestTestHappy {

    private JsonRpcManifestService underTest;

    private ModelIntrospector modelIntrospector;

    @DataProvider
    public static Object[][] getServicesToTest() {
        return new Object[][]{
            {TestJsonRpcServiceSimple.class},
            {TestJsonRpcServiceModelParameters.class},
        };
    }

    @DataProvider
    public static Object[][] getMethodsToTest() {

        final var objects = new ArrayList<Object[]>();
        final var classes = new Class<?>[]{TestJsonRpcServiceSimple.class, TestJsonRpcServiceModelParameters.class};

        for (var cls : classes) {
            for (var method : RemotelyInvokable.Util.getMethods(cls)) {
                objects.add(new Object[]{cls, new MethodWrapper(method)});
            }
        }

        return objects.toArray(Object[][]::new);
    }

    @Test(dataProvider = "getServicesToTest")
    public void testServiceDefinition(final Class<?> cls) {
        final var manifest = getUnderTest().getJsonRpcManifest();
        final var services = manifest.getServicesByName();
        final var service = services.get(RemoteService.Util.getName(cls));
        final var methods = RemotelyInvokable.Util.getMethods(cls);
        assertNotNull(service);
        assertNotNull(service.getDeprecation());
        assertFalse(service.getDeprecation().isDeprecated());
        assertNull(service.getDeprecation().getDeprecationMessage());
        assertEquals(service.getScope(), SimpleJsonRpcManifestTestModule.HAPPY_SCOPE);
        assertEquals(methods.length, service.getJsonRpcMethodList().size());
    }



    @Test(dataProvider = "getMethodsToTest")
    public void testMethods(final Class<?> cls, final MethodWrapper method) {

        final var manifest = getUnderTest().getJsonRpcManifest();
        final var services = manifest.getServicesByName();
        final var service = services.get(RemoteService.Util.getName(cls));
        final var methods = RemotelyInvokable.Util.getMethods(cls);
        assertEquals(methods.length, service.getJsonRpcMethodList().size());

        final var scope = RemoteService.Util.getScope(cls, ELEMENTS_JSON_RPC_HTTP_PROTOCOL, SimpleJsonRpcManifestTestModule.HAPPY_SCOPE);

        final var jrpcMethodName = JVM_NATIVE.methodCaseFormat().to(
            scope.style().methodCaseFormat(),
            method.method.getName()
        );

        final var jrpcMethod = service.getJsonRpcMethodList()
            .stream()
            .filter(m -> m.getName().equals(jrpcMethodName))
            .findFirst()
            .get();

        final var returns = jrpcMethod.getReturns();

        if (void.class.equals(method.method.getReturnType())) {
            assertNull(returns);
        } else {
            final var returnType = getModelIntrospector().introspectClassForType(method.method.getReturnType());
            final var returnModel = getModelIntrospector().introspectClassForModelName(method.method.getReturnType(), scope);
            assertEquals(returnType, returns.getType());
            assertEquals(returnModel, returns.getModel());
        }

        final var parameters = method.method.getParameters();

        for (int index = 0; index < jrpcMethod.getParameters().size(); ++index) {

            final var i = index;
            final var parameter = parameters[index];
            final var jrpcParameter = jrpcMethod
                .getParameters()
                .stream()
                .filter(p -> p.getIndex() == i)
                .findFirst()
                .get();

            final var jrpcParameterName = JVM_NATIVE.parameterCaseFormat().to(scope.style().parameterCaseFormat(), parameter.getName());
            assertEquals(jrpcParameterName, jrpcParameter.getName());

            final var parameterType = getModelIntrospector().introspectClassForType(parameter.getType());
            final var parameterModel = getModelIntrospector().introspectClassForModelName(parameter.getType(), scope);
            assertEquals(parameterType, jrpcParameter.getType());
            assertEquals(parameterModel, jrpcParameter.getModel());

        }

    }

    public JsonRpcManifestService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(JsonRpcManifestService underTest) {
        this.underTest = underTest;
    }

    public ModelIntrospector getModelIntrospector() {
        return modelIntrospector;
    }

    @Inject
    public void setModelIntrospector(ModelIntrospector modelIntrospector) {
        this.modelIntrospector = modelIntrospector;
    }

    private static class MethodWrapper {

        private final Method method;

        public MethodWrapper(Method method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return method.toString();
        }

    }

    public static class Module extends SimpleJsonRpcManifestTestModule {
        @Override
        protected void configureTypes() {
            bindService(TestJsonRpcServiceSimple.class);
            bindService(TestJsonRpcServiceModelParameters.class);
        }
    }

}
