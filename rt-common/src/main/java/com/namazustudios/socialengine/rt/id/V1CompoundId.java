package com.namazustudios.socialengine.rt.id;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.FIELD_COUNT;
import static java.lang.System.arraycopy;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.UUID.fromString;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * An internal use only compound ID.  This manages a set of hard-coded fields and allows for the basic operations of
 * building, parsing, and testing for equality.  This is {@link Serializable} and maintains its mapping as a sorted
 * array of components.
 *
 * This class is immutable, in a style similar to {@link String}.
 */
class V1CompoundId implements Serializable {

    public static final String PREFIX = "V1::";

    public static final byte PREFIX_BYTE = 1;

    public static final String  ID_SEPARATOR = ":";

    public static final Pattern ID_SEPARATOR_PATTERN = compile(ID_SEPARATOR);

    private static final Field[] FIELDS = Field.values();

    private final Component[] components;

    static {

        // This is a check to ensure that, as we add more fields, that the codes will never collide.  This should be
        // caught during integration/unit testing and result in a classloader exception.

        final Set<String> codes = stream(FIELDS).map(f -> f.code).collect(toSet());

        if (codes.size() != FIELDS.length) {
            throw new Error("Detected duplicate field code: " + codes.stream().collect(joining(",")));
        }

    }

    /**
     * Necessary for serialization.
     */
    private V1CompoundId() {
        this.components = new Component[FIELD_COUNT];
    }

    /**
     * Private constructor.  Constructs an instance by copying the array of {@link Component}s into the newly formed
     * object.
     *
     * @param components the {@link Component}s that make up this ID.
     */
    private V1CompoundId(final Component[] components) {
        this.components = new Component[FIELD_COUNT];
        arraycopy(components, 0, this.components, 0, components.length);
    }

    /**
     * Parses out a an instance of {@link V1CompoundId} from the string representation.
     *
     * @param stringRepresentation the string representation
     */
    private V1CompoundId(final String stringRepresentation) {

        components = new Component[FIELD_COUNT];

        if (!stringRepresentation.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid ID: " + stringRepresentation);
        }

        final String componentsString = stringRepresentation.substring(PREFIX.length());

        for(final String componentString : ID_SEPARATOR_PATTERN.split(componentsString)) {
            final Component component = new Component(componentString);
            components[component.field.ordinal()] = component;
        }

    }

    private V1CompoundId(final byte[] byteRepresentation) {

        components = new Component[FIELD_COUNT];

        int index = 0;

        try {

            if (byteRepresentation.length == 0) throw new IllegalArgumentException("Got zero-length byte array.");

            final byte prefix = byteRepresentation[index++];
            if (prefix != PREFIX_BYTE) throw new IllegalArgumentException("Got invalid prefix byte " + prefix);

            while (index < byteRepresentation.length) {

                long upper = 0;
                long lower = 0;

                final UUID uuid;
                final Field field;

                final int ordinal = byteRepresentation[index++];

                field = FIELDS[ordinal];
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 7));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 6));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 5));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 4));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 3));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 2));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 1));
                upper |= ((byteRepresentation[index++] & 0xFFL) << (8 * 0));

                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 7));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 6));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 5));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 4));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 3));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 2));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 1));
                lower |= ((byteRepresentation[index++] & 0xFFL) << (8 * 0));

                uuid = new UUID(upper, lower);
                components[ordinal] = new Component(field, uuid);

            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(ex);
        }

    }

    private V1CompoundId(final ByteBuffer byteBufferRepresentation) {

        components = new Component[FIELD_COUNT];

        try {

            if (byteBufferRepresentation.remaining() == 0) throw new IllegalArgumentException("Got zero-length ByteBuffer");

            final byte prefix = byteBufferRepresentation.get();
            if (prefix != PREFIX_BYTE) throw new IllegalArgumentException("Got invalid prefix byte: " + prefix);

            while (byteBufferRepresentation.hasRemaining()) {
                final Field field = FIELDS[byteBufferRepresentation.get()];
                final long upper = byteBufferRepresentation.getLong();
                final long lower = byteBufferRepresentation.getLong();
                final UUID uuid = new UUID(upper, lower);
                components[field.ordinal()] = new Component(field, uuid);
            }

        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(ex);
        }

    }
    /**
     * Gets the {@link Component} representing the {@link Field}, throwing an {@link IllegalStateException} if the
     * field is not present.
     *
     * @param field the {@link Field} to fetch.
     * @return the {@link Component}, never null.
     */
    Component getComponent(final Field field) {
        final Component component = components[field.ordinal()];
        if (component == null) throw new IllegalStateException("No component for field: " + field);
        return component;
    }

    /**
     * Returns a parseable
     * @return
     */
    String asString() {
        return PREFIX + stream(components)
            .filter(c -> c != null)
            .map(component -> component.asString())
            .collect(joining(ID_SEPARATOR));
    }

    /**
     * Selectively iterates this {@link V1CompoundId} and ensures that each {@link Field} is present and joins by the
     * separator.  This further ensures that the correct ordering is applied and the resulting string may be parsed
     * later to re-build this instance of {@link V1CompoundId}.
     *
     * @param fields the {@link Field}s to consider.
     * @return
     */
    String asString(final Field ... fields) {
        sort(fields);
        return PREFIX + stream(fields)
            .map(field -> getComponent(field).asString())
            .collect(joining(ID_SEPARATOR));
    }

    /**
     * Generates a hash code from this {@link V1CompoundId} using only the {@link Field}s specified.  This ensures that
     * each {@link Field} is present.
     *
     * @param fields the {@link Field}s to consider.
     *
     * @return the hash code.
     */
    int hashCode(final Field ... fields) {
        final Component[] components = new Component[fields.length];
        for (int i = 0; i < fields.length; ++i) components[i] = getComponent(fields[i]);
        return Arrays.hashCode(components);
    }

    /**
     * Tests for equality with another {@link V1CompoundId} considering only the {@link Field}s in the process.  When
     * invoked, this ensures that every requested {@link Field} exists in this instance, but not the other instance.
     *
     * @param other the other {@link V1CompoundId} to consider
     * @param fields the list of {@link Field}s to consider
     * @return true, if equal, false otherwise
     * @throws IllegalStateException if any of the requested {@link Field} instances do not appear in this instance
     */
    boolean equals(final V1CompoundId other, final Field ... fields) {

        for (final Field field : fields) {
            final Component component = getComponent(field);
            if (!Objects.equals(component, other.components[field.ordinal()])) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) return false;
        if (!V1CompoundId.class.equals(object.getClass())) return false;
        final V1CompoundId other = (V1CompoundId)object;
        return Arrays.equals(components, other.components);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(components);
    }

    @Override
    public String toString() {
        return asString();
    }

    public byte[] asBytes(final Field ... fields) {
        final byte[] bytes = new byte[fields.length * (16 + 1) + 1];

        int index = 0;

        bytes[index++] = PREFIX_BYTE;

        for (final Field field : fields) {

            final UUID uuid = getComponent(field).getValue();
            final long upper = uuid.getMostSignificantBits();
            final long lower = uuid.getLeastSignificantBits();

            bytes[index++] = (byte) (field.ordinal() & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 7) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 6) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 5) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 4) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 3) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 2) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 1) & 0xFF);
            bytes[index++] = (byte) (upper >> (8 * 0) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 7) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 6) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 5) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 4) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 3) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 2) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 1) & 0xFF);
            bytes[index++] = (byte) (lower >> (8 * 0) & 0xFF);

        }

        return bytes;

    }

    public void toByteBuffer(final ByteBuffer byteBuffer, final Field ... fields) {

        byteBuffer.put(PREFIX_BYTE);

        for (final Field field : fields) {
            final UUID uuid = getComponent(field).getValue();
            final long upper = uuid.getMostSignificantBits();
            final long lower = uuid.getLeastSignificantBits();
            byteBuffer.putLong(upper).putLong(lower);
        }

    }

    /**
     * Represents the various fields which make up a compound ID.
     */
    enum Field {

        /**
         * Indicates the instance ID.
         */
        INSTANCE,

        /**
         * Indicates the application ID.
         */
        APPLICATION,

        /**
         * Indicates the Resource ID
         */
        RESOURCE,

        /**
         * Indicates the TaskId
         */
        TASK;

        public final String code;

        public static final int FIELD_COUNT = values().length;

        Field() { this.code = name().substring(0, 1); }

    }

    /**
     * Represents a component of the compound ID.  This consists of a {@link Field} and the UUID associated with the
     * particular field.
     *
     * This class is immutable, therefore it's safe to share and pass instances around to other {@link V1CompoundId}s
     */
    static class Component implements Serializable, Comparable<Component> {

        private final UUID value;

        private final Field field;

        private static final Pattern COMPONENT_PATTERN = compile("[IART][0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

        // Necessary for Serialization
        private Component() { value = null; field = null; }

        Component(final String stringRepresentation) {

            if (!COMPONENT_PATTERN.matcher(stringRepresentation).matches()) {
                throw new IllegalArgumentException("Invalid component: " + stringRepresentation);
            }

            final String code = stringRepresentation.substring(0, 1);

            field = stream(FIELDS)
                .filter(f -> f.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid code: " + code));

            value = fromString(stringRepresentation.substring(1));

        }

        Component(final Field field, final UUID value) {
            if (field == null) throw new IllegalArgumentException("Invalid field: " + field);
            if (value == null) throw new IllegalArgumentException("Invalid value: " + value);
            this.field = field;
            this.value = value;
        }

        Field getField() {
            return field;
        }

        UUID getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Component)) return false;
            final Component component = (Component) o;
            return getField() == component.getField() && getValue().equals(component.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getField(), getValue());
        }

        @Override
        public int compareTo(final Component o) {
            return field.compareTo(o.field);
        }

        public String asString() {
            return field.code + value;
        }

    }

    /**
     * Used to build an instance of {@link V1CompoundId}.
     */
    static class Builder {

        private final Component[] components = new Component[FIELD_COUNT];

        /**
         * Parses the contents of a string into this builder assigning all fields to the builder.
         *
         * @param stringRepresentation the string representation of the {@link V1CompoundId}.
         *
         * @return this instance
         */
        Builder with(final String stringRepresentation) {
            return with(new V1CompoundId(stringRepresentation));
        }

        /**
         * Parses the contents of a byte[] into this builder assigning all fields to the builder.
         *
         * @param byteRepresentation the byte representation of the {@link V1CompoundId}.
         *
         * @return this instance
         */
        Builder with(final byte[] byteRepresentation) {
            return with(new V1CompoundId(byteRepresentation));
        }

        /**
         * Prses the contensts of a {@link ByteBuffer} into this builder assigning all fields to the builder.
         *
         * @param byteBufferRepresentation the byte representation of the {@link V1CompoundId}.
         *
         * @return this instance
         */
        Builder with(final ByteBuffer byteBufferRepresentation) {
            return with(new V1CompoundId(byteBufferRepresentation));
        }

        /**
         * Copies the contents of the supplied {@link V1CompoundId} into this builder.
         *
         * @param v1CompoundId the {@link V1CompoundId}
         *
         * @return this instance
         */
        Builder with(final V1CompoundId v1CompoundId) {
            arraycopy(v1CompoundId.components, 0, components, 0, v1CompoundId.components.length);
            return this;
        }

        /**
         * Inserts the particular {@link Field} and {@link UUID} value into this builder, overwriting any previous
         * value silently.
         *
         * @param field the {@link Field}
         * @param value the {@link UUID} value of the field
         * @return this instance
         */
        Builder with(final Field field, final UUID value) {
            with(new Component(field, value));
            return this;
        }

        /**
         * Accepts a {@link Component} into this builder, overwriting any previous value silently.
         * @param component the {@link Component} to insert
         * @return this instance
         */
        Builder with(final Component component) {
            if (component == null) throw new IllegalArgumentException("Component must not be null.");
            components[component.field.ordinal()] = component;
            return this;
        }

        /**
         * Removes the {@link Component}s with the specified {@link Field}s, having no effect on fields that were not
         * already present.
         *
         * @param fields the {@link Field}s to remove
         * @return this instance
         */
        Builder without(final Field ... fields) {
            for (final Field field : fields) components[field.ordinal()] = null;
            return this;
        }

        /**
         * Ensures that the {@link Builder} has only the specified fields, nothing more and nothing less.  If the
         * specified {@link Field} is specified and isn't present an {@link IllegalArgumentException} will be thrown.
         *
         * If a {@link Field} isn't specified and is present, then an {@link IllegalArgumentException} will be thrown.
         *
         * @param fields the {@link Field}s to check
         * @return this instance
         */
        Builder only(final Field ... fields) {

            for (int i = 0; i < components.length; ++i) {

                boolean found = false;
                final Component component = components[i];

                for (int j = 0; j < fields.length; ++j) {

                    if (component == null) continue;

                    final Field field = fields[j];

                    if (component.field.equals(field)) {
                        found = true;
                        break;
                    }

                }

                if (found) {
                    if (components[i] == null) {
                        throw new IllegalArgumentException("Missing Field: " + FIELDS[i]);
                    }
                } else {
                    if (components[i] != null) {
                        throw new IllegalArgumentException("Unexpected Field: " + FIELDS[i]);
                    }
                }

            }

            return this;

        }

        /**
         * Builds the {@link V1CompoundId} from this build.
         *
         * @return a newly build {@link V1CompoundId} instance
         */
        public V1CompoundId build() {
            return new V1CompoundId(components);
        }

    }

}
