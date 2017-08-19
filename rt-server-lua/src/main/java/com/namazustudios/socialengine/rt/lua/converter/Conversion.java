package com.namazustudios.socialengine.rt.lua.converter;

import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/18/17.
 */
public interface Conversion<T> {

    /**
     * Gets the value of this {@link Conversion}.
     *
     * @return
     */
    default T get() {
        return null;
    }

    /**
     * Returns the invalid value of this {@link Conversion}.
     *
     * @return the failed value or null if the conversion was successful
     */
    default Object invalid() {
        return null;
    }

    /**
     * Returns true if the conversion is valid.
     *
     * @return true if the conversion is valid, false otherwise
     */
    default boolean isValid() {
        return true;
    }

    /**
     * If the conversion failed, then this will trap the error and simply return the default value.
     *
     * @param defaultValue the default value
     * @return a Conversion<T> which will succeed or, if not valid, defer to the default value
     */
    default Conversion<T> orElse(final T defaultValue) {
        return new Conversion<T>() {

            @Override
            public T get() {
                return isValid() ? Conversion.this.get() : defaultValue;
            }

            @Override
            public Object invalid() {
                return Conversion.this.invalid();
            }

            @Override
            public boolean isValid() {
                return Conversion.this.isValid();
            }

        };

    }

    /**
     * If invalid data was produced, this will throw the instance provided by the {@link Function}, the function
     * accepts the invalid value as the argument and produces an instance of {@link Throwable} which will be
     * thrown from teh body of this call.
     *
     * @param supplier the {@link Function} which will convert the invalid value to an exception
     * @param <X> the {@link Throwable} type
     *
     * @return the value of {@link #get()}, provided it is valid
     */
    default <X extends RuntimeException> Conversion<T> orThrow(Function<Object, ? extends X> supplier) throws X {
        return new Conversion<T>() {

            @Override
            public T get() {
                if (isValid()) {
                    return Conversion.this.get();
                } else {
                    throw supplier.apply(invalid());
                }
            }

            @Override
            public Object invalid() {
                return Conversion.this.invalid();
            }

            @Override
            public boolean isValid() {
                return Conversion.this.isValid();
            }

        };
    }

    /**
     * Returns a {@link Conversion} which casts to the provided type, considering null as an invalid value.
     *
     * @param uClass the target type
     * @param <U>
     * @return a {@link Conversion<U>} to the specified type.
     */
    default <U> Conversion<U> asCastTo(Class<U> uClass) {
        return asMappedBy(t -> uClass.cast(t), t -> uClass.isInstance(t));
    }

    /**
     * Attempts to convert to any {@link Enum} type using {@link Enum#valueOf(Class, String)}.
     *
     * @param uEnumClass the {@link Class} of the desired {@link Enum}
     * @param <U>
     * @return a {@link Conversion} that enum
     */
    default <U extends Enum<U>> Conversion<U> asEnum(Class<U> uEnumClass) {
        return asCastTo(String.class).asMappedBy(s -> Enum.valueOf(uEnumClass, s));
    }

    /**
     * Attempts to perform mapping by using a {@link Function} to perform the mapping.  This uses the
     * original mapping function to determine if the data is valid.  It does so by catching commonly
     * used exceptions to indicate failure.
     *
     * Specifically, it attempts to invoke the supplied {@link Function}, and attempts to catch
     * {@link ClassCastException}, {@link IllegalArgumentException}, or {@link NullPointerException}.  If
     * any of those exception types are thrown, then it assumes this is normal and marks it as
     * invalid data.
     *
     * Use this with care as you may end up catching exceptions you do not intend to catch.
     *
     * @param function the {@link Function} to perform the mapping
     * @param <U>
     * @return a {@link Conversion} specified by the function's return type.
     */
    default <U> Conversion<U> asMappedBy(final Function<T, U> function) {
        return asMappedBy(function, v -> {
            try {
                function.apply(v);
                return true;
            } catch (ClassCastException | IllegalArgumentException | NullPointerException ex) {
                return false;
            }
        });
    }

    /**
     * Attempts to perform mapping by using a {@link Function} to perform the mapping.
     *
     * @param function the {@link Function} to perform the mapping
     * @param validator a {@link Function} which determines if the mapping is valid
     * @param <U>
     * @return a {@link Conversion} specified by the function's return type.
     */
    default <U> Conversion<U> asMappedBy(final Function<T, U> function,
                                         final Function<T, Boolean> validator) {
        return new Conversion<U>() {

            @Override
            public U get() {
                return isValid() ? function.apply(Conversion.this.get()) : null;
            }

            @Override
            public Object invalid() {
                return !Conversion.this.isValid() ? Conversion.this.invalid() :
                       !isValid()                 ? Conversion.this.get()     :
                                                    null;
            }

            @Override
            public boolean isValid() {
                return Conversion.this.isValid() && validator.apply(Conversion.this.get());
            }

        };

    }

    /**
     * Returns a invalid conversion.  The {@link #isValid()} will always return false
     * and both.
     *
     * @param <U>
     * @return
     */
    static <U> Conversion<U> fail() {
        return fail(null);
    }

    /**
     * Returns a invalid conversion.  The {@link #isValid()} will always return false
     * and the {@link #invalid()} method will return the specified object.
     *
     *
     *
     * @param <U> the object to fail on
     * @return
     */
    static <U> Conversion<U> fail(final U value) {
        return new Conversion<U>() {

            @Override
            public Object invalid() {
                return value;
            }

            @Override
            public boolean isValid() {
                return false;
            }

        };
    }

    /**
     * Creates a new {@link Conversion} from the starting {@link Object}.
     *
     * @param object
     * @return
             */
    static Conversion<Object> from(final Object object) {
        return new Conversion<Object>() {
            @Override
            public Object get() {
                return object;
            }
        };
    }

    /**
     * Returns a {@link Map} that will provide {@link Conversion} instances on the fly.  Useful for picking
     * apart and converting values in a potentially sparse {@link Map}.
     *
     * Unlike {@link Map}, this implementation will return a non-null instance for any key supplied, even
     * if {@link Map#containsKey(Object)} returns false.  The resulting {@link Conversion}, however will
     * always return {@link #fail()}, or a specified default conversion.
     *
     * @param map the {@link Map}
     * @return a {@linK Map<?, Conversion<?>>}
     */
    static Map<?, Conversion<?>> fromMap(final Map map) {

        final Map<Object, Conversion<?>> transformed =  Maps.transformValues(map, Conversion::from);

        return new ForwardingMap<Object, Conversion<?>>() {

            @Override
            protected Map<Object, Conversion<?>> delegate() {
                return transformed;
            }

            @Override
            public Conversion<?> get(@Nullable Object key) {
                return containsKey(key) ? super.get(key) : fail();
            }

            @Override
            public Conversion<?> getOrDefault(Object key, Conversion<?> defaultValue) {
                return containsKey(key)        ? super.get(key) :
                       defaultValue != null    ? defaultValue   :
                                                 fail();
            }

        };

    }

    /**
     * Returns a {@link Collection} that will provide {@link Conversion} instances on the fly.
     *
     * @param collection the {@link Collection} of items to convert
     *
     * @return the {@link Collection<Conversion<?>>} instance
     */
    static Collection<Conversion<?>> fromCollection(final Collection<?> collection) {
        return Collections2.transform(collection, Conversion::from);
    }

}
