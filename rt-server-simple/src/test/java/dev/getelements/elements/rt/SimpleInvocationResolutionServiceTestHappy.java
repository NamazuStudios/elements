package dev.getelements.elements.rt;


import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.exception.ModelNotFoundException;
import dev.getelements.elements.rt.jrpc.JsonRpcInvocationService;
import dev.getelements.elements.rt.jrpc.JsonRpcManifestService;
import dev.getelements.elements.rt.jrpc.JsonRpcRequest;
import dev.getelements.elements.rt.manifest.jrpc.JsonRpcMethod;
import dev.getelements.elements.rt.manifest.jrpc.JsonRpcParameter;
import dev.getelements.elements.rt.manifest.jrpc.JsonRpcService;
import dev.getelements.elements.rt.remote.Invocation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static dev.getelements.elements.rt.SimpleModelManifestService.RPC_MODELS;
import static dev.getelements.elements.rt.annotation.CodeStyle.JVM_NATIVE;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.REMOTE_SCOPE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.*;

@Guice(modules = {
    ValidationModule.class,
    SimpleJsonRpcInvocationServiceTestModule.class,
    SimpleInvocationResolutionServiceTestHappy.Module.class
})
public class SimpleInvocationResolutionServiceTestHappy {

    private String scope;

    private Set<Class<?>> models;

    private PayloadReader payloadReader;

    private JsonRpcManifestService manifestService;

    private JsonRpcInvocationService jsonRpcInvocationService;

    @DataProvider
    public Object[][] getJsonRpcMethods() {
        return getManifestService().getJsonRpcManifest()
            .getServicesByName()
            .values()
            .stream()
            .map(JsonRpcService::getJsonRpcMethodList)
            .flatMap(List::stream)
            .map(m -> new Object[]{m})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getJsonRpcMethods")
    public void testBuildInvocationArray(final JsonRpcMethod jsonRpcMethod) throws Exception {

        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setMethod(jsonRpcMethod.getName());

        final var params = jsonRpcMethod
            .getParameters()
            .stream()
            .map(this::getDefaultParameterValue)
            .collect(toList());

        jsonRpcRequest.setParams(params);

        final var invocation = getJsonRpcInvocationService().resolve(jsonRpcRequest);
        ensureInvocationMethodExists(jsonRpcMethod, invocation.newInvocation());

    }

    @Test(dataProvider = "getJsonRpcMethods")
    public void testBuildInvocationObject(final JsonRpcMethod jsonRpcMethod) throws Exception {

        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setMethod(jsonRpcMethod.getName());

        final var params = jsonRpcMethod
            .getParameters()
            .stream()
            .collect(toMap(JsonRpcParameter::getName, this::getDefaultParameterValue));

        jsonRpcRequest.setParams(params);

        final var invocation = getJsonRpcInvocationService().resolve(jsonRpcRequest);
        ensureInvocationMethodExists(jsonRpcMethod, invocation.newInvocation());

    }
    @DataProvider
    public Object[][] getSingleArgJsonRpcMethods() {
        return getManifestService().getJsonRpcManifest()
            .getServicesByName()
            .values()
            .stream()
            .map(JsonRpcService::getJsonRpcMethodList)
            .flatMap(List::stream)
            .filter(method -> method.getParameters().size() == 1)
            .map(m -> new Object[]{m})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getSingleArgJsonRpcMethods")
    public void testBuildInvocationSingleArg(final JsonRpcMethod jsonRpcMethod) throws Exception {

        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setMethod(jsonRpcMethod.getName());

        final var parameter = jsonRpcMethod.getParameters().get(0);
        final var params = getDefaultParameterValue(parameter);
        jsonRpcRequest.setParams(params);

        final var invocation = getJsonRpcInvocationService().resolve(jsonRpcRequest);
        ensureInvocationMethodExists(jsonRpcMethod, invocation.newInvocation());

    }

    private void ensureInvocationMethodExists(
            final JsonRpcMethod jsonRpcMethod,
            final Invocation invocation) throws Exception {

        final var cls = Class.forName(invocation.getType());
        final var method = findJsonRpcMethod(cls, jsonRpcMethod);

        final var jvmParameters = method.getParameters();
        assertEquals(jvmParameters.length, invocation.getArguments().size());
        assertEquals(jvmParameters.length, invocation.getParameters().size());

        for (int i = 0; i < jvmParameters.length; ++i) {
            assertParameterMatchesArgument(jvmParameters[i], invocation.getArguments().get(i));
            assertEquals(jvmParameters[i].getType().getName(), invocation.getParameters().get(i));
        }

    }

    private Method findJsonRpcMethod(final Class<?> cls, final JsonRpcMethod jsonRpcMethod) {

        final var scope = RemoteService.Util.getScope(cls, ELEMENTS_JSON_RPC_PROTOCOL, getScope());
        final var mcf = scope.style().methodCaseFormat();

        return Reflection
            .methods(cls)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .filter(m -> JVM_NATIVE.methodCaseFormat().to(mcf, m.getName()).equals(jsonRpcMethod.getName()))
            .findFirst()
            .get();

    }

    private void assertParameterMatchesArgument(final Parameter parameter, final Object argument) {

        final var type = parameter.getType();

        if (byte.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Byte.class, argument.getClass());
        } else if (short.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Short.class, argument.getClass());
        } else if (char.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Character.class, argument.getClass());
        } else if (int.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Integer.class, argument.getClass());
        } else if (long.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Long.class, argument.getClass());
        } else if (float.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Float.class, argument.getClass());
        } else if (double.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Double.class, argument.getClass());
        } else if (boolean.class.equals(type)) {
            assertNotNull(argument);
            assertEquals(Boolean.class, argument.getClass());
        } else if (argument != null) {
            assertNotNull(argument);
            assertEquals(type, argument.getClass());
        }

    }

    private Object getDefaultParameterValue(final JsonRpcParameter p) {
        switch (p.getType()) {
            case ARRAY:
                return getDefaultArrayParameterValue(p);
            case OBJECT:
                return getDefaultObjectParameterValue(p);
            case INTEGER:
                return 0;
            case BOOLEAN:
                return true;
            case STRING:
                return "";
            case NUMBER:
                return 0.0;
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + p.getType());
        }
    }

    private Object getDefaultArrayParameterValue(final JsonRpcParameter jsonRpcParameter) {

        final var type = jsonRpcParameter.getType();

        if (type == null) {
            return new ArrayList<>();
        } else {
            try {
                final var cls = findModelNamed(jsonRpcParameter.getName());
                final var ctor = cls.getConstructor();
                final var instance= List.of(ctor.newInstance());
                return getPayloadReader().convert(List.class, instance);
            } catch (Exception ex) {
                fail("Failed to instantiate using reflection.", ex);
                return null;
            }
        }

    }

    private Object getDefaultObjectParameterValue(final JsonRpcParameter jsonRpcParameter) {

        final var model = jsonRpcParameter.getModel();

        if (model == null) {
            return new HashMap<String, Object>();
        } else {
            try {
                final var cls = findModelNamed(model);
                final var ctor = cls.getConstructor();
                final var instance= ctor.newInstance();
                return getPayloadReader().convert(Map.class, instance);
            } catch (Exception ex) {
                fail("Failed to instantiate using reflection.", ex);
                return null;
            }
        }

    }

    private Class<?> findModelNamed(final String name) {
        return getModels()
            .stream()
            .filter(cls -> RemoteModel.Util.getName(cls).equals(name))
            .findAny()
            .orElseThrow(ModelNotFoundException::new);
    }

    public String getScope() {
        return scope;
    }

    @Inject
    public void setScope(@Named(REMOTE_SCOPE) String scope) {
        this.scope = scope;
    }

    public Set<Class<?>> getModels() {
        return models;
    }

    @Inject
    public void setModels(@Named(RPC_MODELS) Set<Class<?>> models) {
        this.models = models;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    @Inject
    public void setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public JsonRpcManifestService getManifestService() {
        return manifestService;
    }

    @Inject
    public void setManifestService(JsonRpcManifestService manifestService) {
        this.manifestService = manifestService;
    }

    public JsonRpcInvocationService getJsonRpcInvocationService() {
        return jsonRpcInvocationService;
    }

    @Inject
    public void setJsonRpcInvocationService(JsonRpcInvocationService jsonRpcInvocationService) {
        this.jsonRpcInvocationService = jsonRpcInvocationService;
    }

    public static class Module extends SimpleJsonRpcManifestTestModule {
        @Override
        protected void configureTypes() {
            bindModel(TestJsonRpcModelA.class);
            bindModel(TestJsonRpcModelB.class);
            bindService(TestJsonRpcServiceSimple.class);
            bindService(TestJsonRpcServiceModelParameters.class);
        }
    }

}
