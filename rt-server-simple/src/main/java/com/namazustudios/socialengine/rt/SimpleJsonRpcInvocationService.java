package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Dispatch;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcManifest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcParameter;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcService;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import com.namazustudios.socialengine.rt.manifest.model.Type;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.util.LazyValue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestService.RPC_SERVICES;
import static com.namazustudios.socialengine.rt.annotation.CodeStyle.JVM_NATIVE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_SCOPE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class SimpleJsonRpcInvocationService implements JsonRpcInvocationService {

    private String scope;

    private String protocol;

    private Validator validator;

    private Set<Class<?>> services;

    private PayloadReader payloadReader;

    private ModelIntrospector modelIntrospector;

    private JsonRpcManifestService jsonRpcManifestService;

    private final LazyValue<JsonRpcManifest> jsonRpcManifest = new LazyValue<>(() -> getJsonRpcManifestService().getJsonRpcManifest());

    private final Map<String, JsonRpcServiceResolution> resolutionCache = new ConcurrentHashMap<>();

    private final Map<JsonRpcMethodCacheKey, Function<JsonRpcRequest, Invocation>> methodCache = new ConcurrentHashMap<>();

    private static final List<Object> NULL_ARG = Arrays.asList(new Object[]{null});

    @Override
    public Invocation resolveInvocation(final JsonRpcRequest rpcRequest) {

        final var violations = getValidator().validate(rpcRequest);

        if (!violations.isEmpty()) {

            final var message = violations
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

            throw new BadRequestException(message);

        }

        final var resolution = resolutionCache.computeIfAbsent(
            rpcRequest.getMethod(),
            JsonRpcServiceResolution::new
        );

        final var key = new JsonRpcMethodCacheKey(rpcRequest, resolution);

        return methodCache
            .computeIfAbsent(key, k -> computeMethodProcessor(k, resolution))
            .apply(rpcRequest);

    }

    private Function<JsonRpcRequest, Invocation> computeMethodProcessor(
            final JsonRpcMethodCacheKey key,
            final JsonRpcServiceResolution resolution) {

        final var argumentConverter = buildArgumentConverter(key, resolution);

        return jsonRpcRequest -> {
            final var invocation = new Invocation();
            final var type = resolution.getServiceClass().getName();
            final var method = resolution.getServiceMethod().getName();
            final var arguments = argumentConverter.apply(jsonRpcRequest);
            invocation.setType(type);
            invocation.setMethod(method);
            invocation.setArguments(arguments);
            invocation.setParameters(resolution.getParameters());
            invocation.setDispatchType(resolution.getDispatchType());
            return invocation;
        };

    }

    private Function<JsonRpcRequest, List<Object>> buildArgumentConverter(
            final JsonRpcMethodCacheKey key,
            final JsonRpcServiceResolution resolution) {

        if (key.getType() == null) {
            return jsonRpcRequest -> emptyList();
        }

        switch (key.getType()) {
            case ARRAY:
                return buildArgumentConverterForArray(key, resolution);
            case OBJECT:
                return buildArgumentConverterForObject(key, resolution);
            default:
                return buildArgumentConverterForSingularParameter(key, resolution);
        }

    }

    private Function<JsonRpcRequest, List<Object>> buildArgumentConverterForArray(
            final JsonRpcMethodCacheKey key,
            final JsonRpcServiceResolution resolution) {

        final var javaParameterTypes = Arrays
            .stream(resolution.getServiceMethod().getParameters())
            .map(Parameter::getType)
            .collect(toList());

        return jsonRpcRequest -> {

            final var jsonParameters = getPayloadReader().convert(List.class, jsonRpcRequest.getParams());

            if (jsonParameters.size() != javaParameterTypes.size()) {
                throw new BadRequestException(
                    "Incorrect parameter count for method " + key.getMethod() +
                    "(expected " + javaParameterTypes.size() + ")."
                );
            }

            return IntStream
                .range(0, javaParameterTypes.size())
                .mapToObj(index -> getPayloadReader().convert(javaParameterTypes.get(index), jsonParameters.get(index)))
                .collect(toList());

        };
    }

    private Function<JsonRpcRequest, List<Object>> buildArgumentConverterForObject(
            final JsonRpcMethodCacheKey key,
            final JsonRpcServiceResolution resolution) {

        final var javaParameters = resolution.getServiceMethod().getParameters();

        return jsonRpcRequest -> {

            final Map<?,?> jsonParameters = getPayloadReader().convert(Map.class, jsonRpcRequest.getParams());

            return Stream
                .of(javaParameters)
                .map(p -> {

                    final var name = resolution
                        .getRemoteScope()
                        .style()
                        .parameterCaseFormat()
                        .to(JVM_NATIVE.parameterCaseFormat(), p.getName());

                    final var value = jsonParameters.get(name);

                    if (jsonParameters.containsKey(name)) {
                        return getPayloadReader().convert(p.getType(), value);
                    } else {
                        return Reflection.getDefaultValue(p.getType());
                    }

                }).collect(toList());

        };
    }

    private Function<JsonRpcRequest, List<Object>> buildArgumentConverterForSingularParameter(
            final JsonRpcMethodCacheKey key,
            final JsonRpcServiceResolution resolution) {

        final var javaParameters = resolution.getServiceMethod().getParameters();

        if (javaParameters.length != 1) {
            throw new BadRequestException(
                "Incorrect parameter count for method " + key.getMethod() +
                "(expected 1)."
            );
        }

        final var jp = javaParameters[0];

        return p ->
            p == null ?                                   List.of(Reflection.getDefaultValue(jp)) :
            jp.getType().isAssignableFrom(p.getClass()) ? List.of(p) :
                                                          List.of(getPayloadReader().convert(jp.getType(), p));

    }

    public String getScope() {
        return scope;
    }

    @Inject
    public void setScope(@Named(REMOTE_SCOPE) String scope) {
        this.scope = scope;
    }

    public String getProtocol() {
        return protocol;
    }

    @Inject
    public void setProtocol(@Named(REMOTE_PROTOCOL) String protocol) {
        this.protocol = protocol;
    }

    public Validator getValidator() {
        return validator;
    }

    @Inject
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public Set<Class<?>> getServices() {
        return services;
    }

    @Inject
    public void setServices(@Named(RPC_SERVICES) Set<Class<?>> services) {
        this.services = services;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    @Inject
    public void setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public ModelIntrospector getModelIntrospector() {
        return modelIntrospector;
    }

    @Inject
    public void setModelIntrospector(ModelIntrospector modelIntrospector) {
        this.modelIntrospector = modelIntrospector;
    }

    public JsonRpcManifestService getJsonRpcManifestService() {
        return jsonRpcManifestService;
    }

    @Inject
    public void setJsonRpcManifestService(JsonRpcManifestService jsonRpcManifestService) {
        this.jsonRpcManifestService = jsonRpcManifestService;
    }

    private class JsonRpcServiceResolution {

        private final Class<?> serviceClass;

        private final Method serviceMethod;

        private final RemoteScope remoteScope;

        private final List<String> parameters;

        private final Dispatch.Type dispatchType;

        public JsonRpcServiceResolution(final String jsonRpcMethod) {
            this.serviceClass = findServiceClass(jsonRpcMethod);
            this.remoteScope = RemoteService.Util.getScope(this.serviceClass, getProtocol(), getScope());
            this.serviceMethod = findServiceMethod(jsonRpcMethod);
            this.dispatchType = Dispatch.Type.determine(this.serviceMethod);
            this.parameters = Stream
                .of(serviceMethod.getParameters())
                .map(p -> p.getType().getName())
                .collect(toList());
        }

        private Method findServiceMethod(final String jsonRpcMethod) {
            return RemotelyInvokable.Util
                .getMethodStream(serviceClass)
                .findFirst()
                .orElseThrow(() -> new MethodNotFoundException("Unable to find method: " + jsonRpcMethod));
        }

        private Class<?> findServiceClass(final String jsonRpcMethod) {
            return jsonRpcManifest.get()
                .getServicesByName()
                .entrySet()
                .stream()
                .filter(entry -> filterServiceByMethodName(entry.getValue(), jsonRpcMethod))
                .flatMap(entry -> filteredServiceClassesByName(entry.getKey()))
                .findFirst()
                .orElseThrow(() -> new MethodNotFoundException("Unable to find method: " + jsonRpcMethod));
        }

        private boolean filterServiceByMethodName(final JsonRpcService service, final String jsonRpcMethod) {
            return service
                .getJsonRpcMethodList()
                .stream()
                .anyMatch(m -> m.getName().equals(jsonRpcMethod));
        }

        private Stream<Class<?>> filteredServiceClassesByName(final String jsonRpcServiceName) {
            return getServices()
                .stream()
                .filter(c -> RemoteService.Util.getName(c).equals(jsonRpcServiceName));
        }

        public Method getServiceMethod() {
            return serviceMethod;
        }

        public Class<?> getServiceClass() {
            return serviceClass;
        }

        public RemoteScope getRemoteScope() {
            return remoteScope;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public Dispatch.Type getDispatchType() {
            return dispatchType;
        }

    }

    private class JsonRpcMethodCacheKey {

        private final Type type;

        private final String method;

        private final List<JsonRpcParameter> parameters;

        public JsonRpcMethodCacheKey(
                final JsonRpcRequest jsonRpcRequest,
                final JsonRpcServiceResolution resolution) {

            final var parameters = jsonRpcRequest.getParams();

            this.method = jsonRpcRequest.getMethod();

            this.type = parameters == null
                ? null
                : getModelIntrospector().introspectClassForType(parameters.getClass());

            this.parameters = parameters == null
                ? null
                : extractParameters(parameters, resolution);

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonRpcMethodCacheKey that = (JsonRpcMethodCacheKey) o;
            return type == that.type && Objects.equals(method, that.method) && Objects.equals(parameters, that.parameters);
        }

        public Type getType() {
            return type;
        }

        public String getMethod() {
            return method;
        }

        public List<JsonRpcParameter> getParameters() {
            return parameters;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, method, parameters);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("JsonRpcMethodCacheKey{");
            sb.append("method='").append(method).append('\'');
            sb.append(", parameters=").append(parameters);
            sb.append('}');
            return sb.toString();
        }

        private List<JsonRpcParameter> extractParameters(final Object parameters,
                                                         final JsonRpcServiceResolution resolution) {
            switch (type) {
                case ARRAY:
                    return extractParametersArray(parameters, resolution);
                case OBJECT:
                    return extractParametersObject(parameters, resolution);
                default:
                    return anonymousSingleParameter(parameters, resolution);
            }
        }

        private List<JsonRpcParameter> extractParametersArray(final Object parameters,
                                                              final JsonRpcServiceResolution resolution) {

            final var list = getPayloadReader().convert(List.class, parameters);
            final var scope = resolution.getRemoteScope();

            return IntStream
                .range(0, list.size())
                .mapToObj(index -> {
                    final var inputParameter = list.get(index);
                    final var jsonRpcParameter = introspectParameterForType(inputParameter, scope);
                    jsonRpcParameter.setIndex(index);
                    return jsonRpcParameter;
                })
                .collect(toList());

        }

        private List<JsonRpcParameter> extractParametersObject(final Object parameters,
                                                               final JsonRpcServiceResolution resolution) {

            final Map<?,?> map = getPayloadReader().convert(Map.class, parameters);
            final RemoteScope scope = resolution.getRemoteScope();

            return map
                .entrySet()
                .stream()
                .map(entry -> {
                    final var inputParameter = entry.getValue();
                    final var jsonRpcParameter = introspectParameterForType(inputParameter, scope);
                    jsonRpcParameter.setName(entry.getKey().toString());
                    return jsonRpcParameter;
                })
                .collect(toList());

        }

        private List<JsonRpcParameter> anonymousSingleParameter(
                final Object parameters,
                final JsonRpcServiceResolution resolution) {
            final RemoteScope scope = resolution.getRemoteScope();
            return List.of(introspectParameterForType(parameters, scope));
        }

        private JsonRpcParameter introspectParameterForType(final Object inputParameter, final RemoteScope scope) {

            final JsonRpcParameter jsonRpcParameter = new JsonRpcParameter();

            if (inputParameter == null) {
                final var cls = inputParameter.getClass();
                final var type = getModelIntrospector().introspectClassForType(cls);
                final var model = getModelIntrospector().introspectClassForModelName(cls, scope);
                jsonRpcParameter.setType(type);
                jsonRpcParameter.setModel(model);
            }

            return jsonRpcParameter;

        }

    }

}
