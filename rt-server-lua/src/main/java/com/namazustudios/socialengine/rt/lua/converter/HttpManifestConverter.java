package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.rt.ParameterizedPath;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.manifest.http.*;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class HttpManifestConverter extends AbstractMapConverter<HttpManifest> {

    /**
     * The name of the lua module specified in the lua code.  Corresponds to the value
     * of {@link HttpModule#getModule()}.
     */
    public static final String MODULE_KEY = "module";

    /**
     * The name of the lua module specified in the lua code.  Corresponds to the value
     * of {@link HttpModule#getOperationsByName()}.
     */
    public static final String OPERATIONS_KEY = "operations";

    public static final String VERB_KEY = "verb";

    public static final String PATH_KEY = "path";

    public static final String METHOD_KEY = "method";

    public static final String PRODUCES_KEY = "produces";

    public static final String CONSUMES_KEY = "consumes";

    public static final String HEADERS_KEY = "headers";

    public static final String MODEL_KEY = "model";

    public static final String PARAMETERS_KEY = "parameters";

    public static final String DESCRIPTION_KEY = "description";

    @Override
    public Class<HttpManifest> getConvertedType() {
        return HttpManifest.class;
    }

    @Override
    public HttpManifest convertLua2Java(Map<?, ?> map) {
        final Map<String, HttpModule> modulesByName = toModulesByName(map);
        final HttpManifest httpManifest = new HttpManifest();
        httpManifest.setModulesByName(modulesByName);
        return httpManifest;
    }

    private Map<String, HttpModule> toModulesByName(final Map<?, ?> map) {
        final Map<String, HttpModule> modulesByName = new HashMap<>();
        map.forEach((k,v) -> modulesByName.put(k.toString(), toHttpModule(k, v)));
        return modulesByName;
    }

    private HttpModule toHttpModule(final Object key, final Object value) {

        final String module = Conversion.from(key)
            .asCastTo(String.class)
            .orThrow(v -> new BadManifestException("Got non-string value string for module name: " + key))
            .get();

        final Map<?, ?> moduleMap = Conversion.from(value)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Got non-table value module definition: " + key))
            .get();

        final Map<?, Conversion<?>> moduleConversionMap = Conversion.fromMap(moduleMap);

        final Map<?,? > operations = moduleConversionMap
            .get(OPERATIONS_KEY)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Got non-table value for: " + OPERATIONS_KEY))
            .get();

        final Map<String, HttpOperation> operationsByName = toOperationsByName(operations);

        final HttpModule httpModule = new HttpModule();
        httpModule.setModule(moduleConversionMap.get(MODULE_KEY).asCastTo(String.class).orElse(module).get());
        httpModule.setOperationsByName(operationsByName);
        return httpModule;

    }

    private Map<String, HttpOperation> toOperationsByName(Map<?, ?> operations) {
        final Map<String, HttpOperation> operationsByName = new HashMap<>();
        operations.forEach((k,v) -> operationsByName.put(k.toString(), toHttpOperation(k, v)));
        return operationsByName;
    }

    private HttpOperation toHttpOperation(final Object key, final Object value) {

        final String operation = Conversion.from(key)
            .asCastTo(String.class)
            .orThrow(v -> new BadManifestException("Got non-string value string for operation name: " + key))
            .get();

        final Map<?, ?> operationMap = Conversion.from(value)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Got non-table value for operation definition: " + key))
            .get();

        final Map<?, Conversion<?>> operationConversionMap = Conversion.fromMap(operationMap);

        final HttpVerb verb = operationConversionMap.get(VERB_KEY)
            .asEnum(HttpVerb.class)
            .orThrow(v -> new BadManifestException("Unexpected HTTP verb: " + v))
            .get();

        final ParameterizedPath parameterizedPath = operationConversionMap.get(PATH_KEY)
            .asCastTo(String.class)
            .asMappedBy(Path::new)
            .asMappedBy(ParameterizedPath::new)
            .orThrow(v -> new BadManifestException("Invalid Parameterized Path: " + v))
            .get();

        final String description = operationConversionMap.get(DESCRIPTION_KEY)
            .asCastTo(String.class)
            .orElse("")
            .get();

        final String method = operationConversionMap.get(METHOD_KEY)
            .asCastTo(String.class)
            .orThrow(v -> new BadManifestException("Invalid method HTTP operation method name: " + v))
            .get();

        final Map<?, ?> consumes = operationConversionMap.get(CONSUMES_KEY)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Expected table for 'consumes' " + v))
            .get();

        final Map<?, ?> produces = operationConversionMap.get(PRODUCES_KEY)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Expected table list for 'consumes' " + v))
            .get();

        final Map<?, ?> parametersMap = operationConversionMap.get(PARAMETERS_KEY)
            .asCastTo(Map.class)
            .orThrow(v -> new BadManifestException("Expected table for 'parameters' " + v))
            .get();

        final HttpOperation httpOperation = new HttpOperation();
        httpOperation.setName(operation);
        httpOperation.setDescription(description);
        httpOperation.setVerb(verb);
        httpOperation.setPath(parameterizedPath);
        httpOperation.setMethod(method);
        httpOperation.setParameters(toParametersMap(parametersMap));
        httpOperation.setConsumesContentByType(toContentList(consumes, CONSUMES_KEY));
        httpOperation.setProducesContentByType(toContentList(produces, PRODUCES_KEY));
        return httpOperation;

    }

    private Map<String, HttpContent> toContentList(final Map<?, ?> map, final String context) {

        final Map<String, HttpContent> contentByType = new HashMap<>();
        map.forEach((k,v) -> contentByType.put(k.toString(), toContent(k, v, context)));

        final long defaultCount = contentByType.values().stream()
            .filter(c -> c.isDefaultContent())
            .count();

        if (contentByType.isEmpty()) {
            throw new BadManifestException("At least one Content-Type must be specified in " + context);
        } else if (defaultCount > 1) {
            throw new BadManifestException("Only one default content type may be specified in " + context);
        } else if (defaultCount < 1 && contentByType.size() == 1) {
            final HttpContent defaultContent = contentByType.values().iterator().next();
            defaultContent.setDefaultContent(true);
        }

        return contentByType;

    }

    private HttpContent toContent(final Object key, final Object value, final String context) {

        final String type = Conversion.from(key)
            .asCastTo(String.class)
            .orThrow(v -> new BadManifestException("Expected string for key in '" + context + "': "))
            .get();

        final Map<?, Conversion<?>> contentConversionMap = Conversion.fromMap(
            Conversion.from(value)
              .asCastTo(Map.class)
              .orThrow(v -> new BadManifestException("Expected value for in '" + context + "': "))
              .get());

        final Collection<Conversion<?>> headerConversionCollection = Conversion.fromCollection(
            contentConversionMap.get(HEADERS_KEY)
                .asCastTo(Map.class)
                .orThrow(v -> new BadManifestException("Expected table for headers in '" + context + "': " + v))
                .get().values());

        final String model = contentConversionMap
                .get(MODEL_KEY)
                .asCastTo(String.class)
                .orThrow(v -> new BadManifestException("Expected string for model in '" + context + "': "))
                .get();

        final List<String> headers = headerConversionCollection
            .stream()
            .map(c -> c.asCastTo(String.class)
                       .orThrow(v -> new BadManifestException("Expected string for headers in '" + context + "': " + v))
                       .get())
            .collect(Collectors.toList());

        final HttpContent httpContent = new HttpContent();
        httpContent.setType(type);
        httpContent.setModel(model);
        httpContent.setHeaders(headers);
        return httpContent;

    }

    private Map<String, Type> toParametersMap(final Map<?, ?> map) {
        final Map<String, Type> parameters = new HashMap<>();

        map.forEach((key, value) -> {

            final String name = Conversion.from(key)
                .asCastTo(String.class)
                .orThrow(v -> new BadManifestException("Invalid parameter name: " + v))
                .get();

            final Type type = Conversion.from(value)
                .asCastTo(String.class)
                .asMappedBy(Type::findByValue, v -> stream(Type.values()).anyMatch(e -> e.value.equals(v)))
                .orThrow(v -> new BadManifestException("Invalid parameter type: " + v))
                .get();

            parameters.put(name, type);

        });

        return parameters;
    }

}