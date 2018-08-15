package com.namazustudios.socialengine.client.util;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.EnumMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlaceRequestParameters<ParameterEnumT extends Enum<ParameterEnumT>> {

    public static final String GLOBAL_DEFAULT = "";

    private final EnumMap<ParameterEnumT, Supplier<String>> values;

    private final EnumMap<ParameterEnumT, Function<ParameterEnumT, Boolean>> undefined;

    private PlaceRequestParameters(final EnumMap<ParameterEnumT, Supplier<String>> values,
                                   final EnumMap<ParameterEnumT, Function<ParameterEnumT, Boolean>> undefined) {
        this.values = values;
        this.undefined = undefined;
    }

    public String get(final ParameterEnumT enumT) {
        return values.get(enumT).get();
    }

    public boolean check(final ParameterEnumT enumT) {
        return undefined.get(enumT).apply(enumT);
    }

    public static <ParameterEnumT extends Enum<ParameterEnumT>>
    Builder<ParameterEnumT> builder(final Class<ParameterEnumT> tClass) {
        return new Builder<>(tClass);
    }

    public static class Builder<ParameterEnumT extends Enum<ParameterEnumT>>  {

        private final Class<ParameterEnumT> parameterEnumTClass;

        private final EnumMap<ParameterEnumT, String> defaults;

        private final EnumMap<ParameterEnumT, Function<ParameterEnumT, Boolean>> required;

        private Function<ParameterEnumT, String> extractor = e -> e.name().toLowerCase().trim();

        public Builder(final Class<ParameterEnumT> parameterEnumTClass) {
            this.parameterEnumTClass = parameterEnumTClass;
            this.defaults = new EnumMap<>(parameterEnumTClass);
            this.required = new EnumMap<>(parameterEnumTClass);
        }

        public Builder<ParameterEnumT> extract(final Function<ParameterEnumT, String> extractor) {

            if (extractor == null) {
                throw new IllegalArgumentException("Unspecified extractor.");
            }

            this.extractor = extractor;
            return this;

        }

        public Builder<ParameterEnumT> require(final ParameterEnumT enumT) {
            return require(enumT, e -> false);
        }

        public Builder<ParameterEnumT> require(final ParameterEnumT enumT, final Runnable onFailure) {
            return require(enumT, e -> {onFailure.run(); return false;} );
        }

        public Builder<ParameterEnumT> require(final ParameterEnumT enumT, final Function<ParameterEnumT, Boolean> onFailure) {

            if (required.putIfAbsent(enumT, onFailure) != null) {
                throw new IllegalArgumentException("Already required: " + enumT);
            }

            return this;

        }

        public Builder<ParameterEnumT> withDefault(final ParameterEnumT enumT, final String defaultValue) {

            if (defaults.putIfAbsent(enumT, defaultValue) != null) {
                throw new IllegalArgumentException("Default already specified: " + enumT);
            }

            return this;

        }

        public PlaceRequestParameters<ParameterEnumT> build(final PlaceRequest placeRequest) {

            final EnumMap<ParameterEnumT, Supplier<String>> values = new EnumMap<>(parameterEnumTClass);
            final EnumMap<ParameterEnumT, Function<ParameterEnumT, Boolean>> undefined = new EnumMap<>(parameterEnumTClass);

            final Set<String> parameterNames = placeRequest.getParameterNames();

            for (final ParameterEnumT param : parameterEnumTClass.getEnumConstants()) {

                final String paramName = extractor.apply(param);
                final String defaultValue = defaults.getOrDefault(param, GLOBAL_DEFAULT);

                values.put(param, () -> placeRequest.getParameter(paramName, defaultValue).trim());

                if (parameterNames.contains(paramName)) {
                    undefined.put(param, e -> true);
                } else {
                    undefined.put(param, required.getOrDefault(param, e -> true));
                }

            }

            return new PlaceRequestParameters<>(values, undefined);

        }

    }

}
