package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcManifestService;
import com.namazustudios.socialengine.rt.manifest.Deprecation;
import com.namazustudios.socialengine.rt.manifest.jrpc.*;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.annotation.CodeStyle.JVM_NATIVE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_SCOPE;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class SimpleJsonRpcManifestService implements JsonRpcManifestService {

    public static final String RPC_SERVICES = "com.namazustudios.socialengine.rt.rpc.service.classes";

    private final String scope;

    private final String protocol;

    private final Set<Class<?>> jsonRpcServices;

    private final Validator validator;

    private final PayloadReader payloadReader;

    private final ModelIntrospector modelIntrospector;

    private final JsonRpcManifest jsonRpcManifest;

    @Inject
    public SimpleJsonRpcManifestService(
            @Named(REMOTE_SCOPE)
            final String scope,
            @Named(REMOTE_PROTOCOL)
            final String protocol,
            @Named(RPC_SERVICES)
            final Set<Class<?>> jsonRpcServices,
            final Validator validator,
            final PayloadReader payloadReader,
            final ModelIntrospector modelIntrospector) {

        this.scope = scope;
        this.protocol = protocol;
        this.jsonRpcServices = jsonRpcServices;
        this.validator = validator;
        this.payloadReader = payloadReader;
        this.modelIntrospector = modelIntrospector;

        final var builder = new JsonRpcManifestBuilder();
        this.jsonRpcManifest = builder.jsonRpcManifest;

    }

    @Override
    public JsonRpcManifest getJsonRpcManifest() {
        return getPayloadReader().convert(JsonRpcManifest.class, jsonRpcManifest);
    }

    public Set<Class<?>> getJsonRpcServices() {
        return jsonRpcServices;
    }

    public String getScope() {
        return scope;
    }

    public String getProtocol() {
        return protocol;
    }

    public Validator getValidator() {
        return validator;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    public ModelIntrospector getModelIntrospector() {
        return modelIntrospector;
    }

    private class JsonRpcManifestBuilder {

        private final Set<String> methodNames = new HashSet<>();

        private final JsonRpcManifest jsonRpcManifest = new JsonRpcManifest();

        public JsonRpcManifestBuilder() {

            final var services = new LinkedHashMap<String, JsonRpcService>();

            for (final var cls : getJsonRpcServices()) {

                final var name = RemoteService.Util.getName(cls);
                final var service = buildJsonRpcService(cls);

                if (services.put(name, service) != null) {
                    throw new BadManifestException("Service already exists with name: " + name);
                }

            }

            jsonRpcManifest.setServicesByName(services);

            final var violations = getValidator().validate(jsonRpcManifest);

            if (!violations.isEmpty()) {
                throw new BadManifestException(violations);
            }

        }

        private JsonRpcService buildJsonRpcService(final Class<?> cls) {

            final var jsonRpcService = new JsonRpcService();

            final var rsd= RemoteService.Util.getScope(cls, getProtocol(), getScope());
            jsonRpcService.setScope(rsd.scope());
            jsonRpcService.setDeprecation(Deprecation.from(rsd.deprecated()));

            final var jsonRpcMethodList = buildMethodsForClass(rsd, cls);

            jsonRpcMethodList.forEach(m -> {
                if (!methodNames.add(m.getName())) {
                    throw new BadManifestException("JSON RPC Method Already exists:" + m.getName());
                }
            });

            jsonRpcService.setJsonRpcMethodList(jsonRpcMethodList);

            return jsonRpcService;

        }

        private List<JsonRpcMethod> buildMethodsForClass(final RemoteScope remoteScope, final Class<?> cls) {
            return RemotelyInvokable.Util.getMethodStream(cls)
                .flatMap(method -> buildMethodsForService(remoteScope, method))
                .collect(toList());
        }

        private Stream<JsonRpcMethod> buildMethodsForService(final RemoteScope remoteScope, final Method method) {

            final var methodCaseFormat = remoteScope.style().methodCaseFormat();

            return Stream.of(method.getAnnotationsByType(RemotelyInvokable.class))
                .map(remotelyInvokable -> {
                    final var jsonRpcMethod = new JsonRpcMethod();
                    final var methodName = JVM_NATIVE.methodCaseFormat().to(methodCaseFormat, method.getName());
                    final var methodParameters = buildParameters(remoteScope, method);
                    jsonRpcMethod.setName(methodName);
                    jsonRpcMethod.setParameters(methodParameters);
                    jsonRpcMethod.setDeprecation(Deprecation.from(remotelyInvokable.deprecated()));

                    if (method.getReturnType() != void.class) {
                        final var jsonRpcReturnType = new JsonRpcReturnType();
                        final var returnType = getModelIntrospector().introspectClassForType(method.getReturnType());
                        final var returnModel = getModelIntrospector().introspectClassForModelName(method.getReturnType(), remoteScope);
                        jsonRpcReturnType.setType(returnType);
                        jsonRpcReturnType.setModel(returnModel);
                        jsonRpcMethod.setReturns(jsonRpcReturnType);
                    }

                    return jsonRpcMethod;
                });

        }

        private List<JsonRpcParameter> buildParameters(
                final RemoteScope remoteScope,
                final Method method) {

            final var parameters = method.getParameters();
            final var jsonRpcParameters = new ArrayList<JsonRpcParameter>();

            int rpcParameterIndex = 0;

            for (var parameterIndex : Reflection.indices(method, Serialize.class)) {

                final var parameter = parameters[parameterIndex];
                final var jsonRpcParameter = new JsonRpcParameter();

                final var name = Serialize.Util
                    .findName(parameter, remoteScope.style())
                    .orElseThrow(() -> new BadManifestException(format(
                        "Unable to determine parameter %d for method %s.",
                        parameterIndex,
                        method.toGenericString()
                    )));

                final var type = getModelIntrospector().introspectClassForType(parameter.getType());

                jsonRpcParameter.setType(type);
                jsonRpcParameter.setName(name);
                jsonRpcParameter.setIndex(rpcParameterIndex++);

                RemoteModel.Util.findScope(parameter.getType(), getProtocol(), getScope()).ifPresent(s -> {
                    final var model = getModelIntrospector().introspectClassForModelName(parameter.getType(), s);
                    jsonRpcParameter.setModel(model);
                });

                jsonRpcParameters.add(jsonRpcParameter);

            }

            jsonRpcParameters.sort(comparing(JsonRpcParameter::getIndex));
            return jsonRpcParameters;

        }

    }

}
