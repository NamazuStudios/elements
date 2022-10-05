package com.namazustudios.socialengine.rt.transact;


import com.namazustudios.socialengine.rt.JsonRpcInvocationService;
import com.namazustudios.socialengine.rt.JsonRpcManifestService;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.exception.ModelNotFoundException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcMethod;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcParameter;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.namazustudios.socialengine.rt.SimpleModelManifestService.RPC_MODELS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.fail;

@Guice(modules = {
    ValidationModule.class,
    SimpleJsonRpcInvocationServiceTestModule.class,
    SimpleJsonRpcInvocationServiceTestHappy.Module.class
})
public class SimpleJsonRpcInvocationServiceTestHappy {

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
    public void testBuildInvocationArray(final JsonRpcMethod jsonRpcMethod) {

        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setMethod(jsonRpcMethod.getName());

        final var params = jsonRpcMethod
            .getParameters()
            .stream()
            .map(this::getDefaultParameterValue)
            .collect(toList());

        jsonRpcRequest.setParams(params);

        final var invocation = getJsonRpcInvocationService().resolveInvocation(jsonRpcRequest);
        invocation.getParameters();
        invocation.getDispatchType();

    }

    @Test(dataProvider = "getJsonRpcMethods")
    public void testBuildInvocationObject(final JsonRpcMethod jsonRpcMethod) {

        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setMethod(jsonRpcMethod.getName());

        final var params = jsonRpcMethod
            .getParameters()
            .stream()
            .collect(toMap(JsonRpcParameter::getName, this::getDefaultParameterValue));

        jsonRpcRequest.setParams(params);

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

        final var type = jsonRpcParameter.getType();

        if (type == null) {
            return new HashMap<String, Object>();
        } else {
            try {
                final var cls = findModelNamed(jsonRpcParameter.getName());
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
