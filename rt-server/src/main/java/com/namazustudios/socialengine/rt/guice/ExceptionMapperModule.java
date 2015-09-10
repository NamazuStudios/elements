package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.namazustudios.socialengine.rt.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 9/9/15.
 */
public class ExceptionMapperModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionMapperModule.class);

    private final Map<Class<?>, Provider<ExceptionMapper<?>>> exceptionMapperProviders = new LinkedHashMap<>();

    @Override
    protected final void configure() {

        final Matcher<TypeLiteral<?>> matcher = new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeLiteral) {
                return ExceptionMapper.class.isAssignableFrom(typeLiteral.getRawType());
            }
        };

        binder().bindListener(matcher, new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {

                for (final Type genericInterface : type.getRawType().getGenericInterfaces()) {

                    if (!(genericInterface instanceof ParameterizedType)) {
                        LOG.trace("Skipping generic interface {} ", genericInterface);
                        continue;
                    }

                    final ParameterizedType parameterizedType = (ParameterizedType)genericInterface;

                    if (!(parameterizedType.getRawType() instanceof  Class)) {
                        LOG.trace("Skipping generic interface {} ", genericInterface);
                        continue;
                    } else if (!ExceptionMapper.class.isAssignableFrom((Class<?>)parameterizedType.getRawType())) {
                        LOG.trace("Skipping generic interface {} with raw type {}", genericInterface, parameterizedType.getRawType());
                        continue;
                    }

                    final Type exceptionType = parameterizedType.getActualTypeArguments()[0];

                    if (!(exceptionType instanceof Class<?>)) {
                        LOG.trace("Skipping generic interface {} with type parameter {} ", genericInterface, exceptionType);
                        continue;
                    }

                    LOG.info("Mapping {} to {} ", exceptionType, type.getRawType());
                    final Provider<I> provider = encounter.getProvider(Key.get(type));

                    // CAST DAMMIT CAST
                    exceptionMapperProviders.put((Class<?>)exceptionType, (Provider<ExceptionMapper<?>>) provider);

                }
            }
        });

        binder().bind(ExceptionMapper.Resolver.class).toInstance(new ExceptionMapper.Resolver() {

            @Override
            public <ExceptionT extends Exception> ExceptionMapper<ExceptionT> getExceptionMapper(ExceptionT ex) {

                Class<?> cls = ex.getClass();

                do {

                    final Provider<ExceptionMapper<?>> exceptionMapperProvider = exceptionMapperProviders.get(cls);

                    if (exceptionMapperProvider != null) {
                        return (ExceptionMapper<ExceptionT>) exceptionMapperProvider.get();
                    }

                } while (!Throwable.class.equals(cls));

                return null;

            }
        });

        configureExceptionMappers();

    }

    /**
     * Can optionally be overridden to add exception mappers.  This is not strictly
     * necessary as this object will register a {@link com.google.inject.spi.TypeListener} to listen
     * for all bound instances of {@link ExceptionMapper} and provide them using the {@link Injector}
     */
    protected void configureExceptionMappers() {}

}
