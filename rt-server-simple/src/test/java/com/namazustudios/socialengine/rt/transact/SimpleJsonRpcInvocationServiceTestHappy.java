package com.namazustudios.socialengine.rt.transact;


import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.exception.ModelNotFoundException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcMethod;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcParameter;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcService;
import com.namazustudios.socialengine.rt.remote.Invocation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.*;

import static com.namazustudios.socialengine.rt.SimpleModelManifestService.RPC_MODELS;
import static com.namazustudios.socialengine.rt.annotation.CodeStyle.JVM_NATIVE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_HTTP_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_SCOPE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@Guice(modules = {
    ValidationModule.class,
    SimpleJsonRpcInvocationServiceTestModule.class,
    SimpleJsonRpcInvocationServiceTestHappy.Module.class
})
public class SimpleJsonRpcInvocationServiceTestHappy {

    private static  final Map<String, Class<?>> PRIMITIVES = Map.of(
        byte.class.getName(),    byte.class,
        short.class.getName(),   short.class,
        char.class.getName(),    char.class,
        int.class.getName(),     int.class,
        boolean.class.getName(), boolean.class,
        long.class.getName(),    long.class,
        float.class.getName(),   float.class,
        double.class.getName(),  double.class
    );

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

        final var invocation = getJsonRpcInvocationService().resolveInvocation(jsonRpcRequest);
        final var method = ensureInvocationMethodExists(jsonRpcMethod, invocation);

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

        final var invocation = getJsonRpcInvocationService().resolveInvocation(jsonRpcRequest);
        final var method = ensureInvocationMethodExists(jsonRpcMethod, invocation);

    }

    private Method ensureInvocationMethodExists(final JsonRpcMethod jsonRpcMethod, final Invocation invocation) throws Exception {
        // Ensures that the Class<?> exists and Method exists
        final var cls = Class.forName(invocation.getType());
        return findJsonRpcMethod(cls, jsonRpcMethod);
    }

    private Method findJsonRpcMethod(final Class<?> cls, final JsonRpcMethod jsonRpcMethod) {

        final var scope = RemoteService.Util.getScope(cls, ELEMENTS_JSON_RPC_HTTP_PROTOCOL, getScope());
        final var mcf = scope.style().methodCaseFormat();

        return Reflection
            .methods(cls)
            .filter(m -> m.getAnnotation(RemotelyInvokable.class) != null)
            .filter(m -> JVM_NATIVE.methodCaseFormat().to(mcf, m.getName()).equals(jsonRpcMethod.getName()))
            .findFirst()
            .get();

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
