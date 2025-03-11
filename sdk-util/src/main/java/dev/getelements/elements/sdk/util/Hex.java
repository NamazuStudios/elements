package dev.getelements.elements.sdk.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Hex {

    /**
     * The case (upper or lower case).
     */
    public enum Case {

        /**
         * Upper case.
         */
        UPPER,

        /**
         * Lower case.
         */
        LOWER
    }

    public static final String VALID_REGEX = "[0-9a-fA-F]*";

    public static final Pattern VALID_PATTERN = Pattern.compile(VALID_REGEX);

    private static final char[] HEX_CHARS_UPPER = "0123456789ABCDEF".toCharArray();

    private static final char[] HEX_CHARS_LOWER = "0123456789abcdef".toCharArray();

    /**
     * Gets a characte for the supplied nibble.
     *
     * @param nibble the nibble
     * @param c the desired case format
     * @return the char for the nibble.
     */
    public static char forNibble(final int nibble, final Case c) {

        final char[] chars;

        switch (c == null ? Case.LOWER : c) {
            case LOWER:
                chars = HEX_CHARS_UPPER;
                break;
            case UPPER:
                chars = HEX_CHARS_LOWER;
                break;
            default:
                throw new IllegalArgumentException("Undefined case " + c);
        }

        if (nibble > chars.length)
            throw new IllegalArgumentException(format("Invalid nibble %X", nibble));

        return chars[nibble];

    }

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @return the {@link String} representing the hex string
     */
    public static String encode(final byte[] raw) {
        return encode(ByteBuffer.wrap(raw), Case.LOWER);
    }

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @param c the {@link Case}
     * @return the {@link String} representing the hex string
     */
    public static String encode(final byte[] raw, final Case c) {
        return encode(ByteBuffer.wrap(raw), c);
    }

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @return the {@link String} representing the hex string
     */
    public static String encode(final ByteBuffer raw) {
        return encode(raw, Case.LOWER);
    }

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @param c the {@link Case}
     * @return the {@link String} representing the hex string
     */
    public static String encode(final ByteBuffer raw, final Case c) {
        switch (c == null ? Case.LOWER : c) {
            case LOWER: return encode(raw, HEX_CHARS_LOWER);
            case UPPER: return encode(raw, HEX_CHARS_UPPER);
            default:    throw new IllegalArgumentException("Undefined case " + c);
        }
    }

    private static String encode(final ByteBuffer bytes, final char[] lookup) {

        final CharBuffer chars = CharBuffer.allocate(bytes.remaining() * 2);

        while(bytes.hasRemaining()) {
            int value = bytes.get() & 0xFF;
            chars.put(lookup[value >>> 4]);
            chars.put(lookup[value & 0xF]);
        }

        chars.flip();
        return chars.toString();

    }

    /**
     * Decodes a {@link String} into raw hexadecimal bytes.
     *
     * @param hex the hex string
     * @return a {@link byte[]} representing the hex
     */
    public static byte[] decode(final String hex) {
        return decodeToBuffer(hex, ByteBuffer::allocate).array();
    }

    /**
     * Decodes a {@link String} into raw hexadecimal bytes, allocating a new {@link ByteBuffer} for storage.
     *
     * @param hex the hex string
     * @return a {@link byte[]} representing the hex
     */
    public static ByteBuffer decodeToBuffer(final String hex) {
        return decodeToBuffer(hex, ByteBuffer::allocate);
    }

    /**
     * Decodes a {@link String} into raw hexadecimal bytes, allocating a new {@link ByteBuffer} for storage using the
     * supplied {@link ByteBufferAllocator}.
     *
     * @param hex the hex string
     * @return a {@link byte[]} representing the hex
     */
    public static ByteBuffer decodeToBuffer(final String hex, final ByteBufferAllocator byteBufferAllocator) {
        return decodeToBuffer(hex, byteBufferAllocator.allocate(hex.length() / 2));
    }

    /**
     * Decodes a {@link String} into raw hexadecimal bytes, allocating a {@link ByteBuffer}.
     *
     * @param hex the hex string
     * @return a {@link byte[]} representing the hex
     * @throws java.nio.BufferOverflowException if the buffer has insufficient space.
     */
    private static ByteBuffer decodeToBuffer(final String hex, final ByteBuffer destination) {

        if ((hex.length() % 2) == 1)
            throw new InvalidHexString("Invalid hex string " + hex);

        final CharBuffer chars = CharBuffer.wrap(hex);

        while (chars.hasRemaining()) {
            final int upper = decode(chars.get()) << 4;
            final int lower = decode(chars.get()) & 0xFF;
            destination.put((byte)(upper | lower));
        }

        return destination;

    }

    private static int decode(final char ch) {
        switch (ch) {
            case '0': return 0x0;
            case '1': return 0x1;
            case '2': return 0x2;
            case '3': return 0x3;
            case '4': return 0x4;
            case '5': return 0x5;
            case '6': return 0x6;
            case '7': return 0x7;
            case '8': return 0x8;
            case '9': return 0x9;
            case 'a':
            case 'A': return 0xA;
            case 'b':
            case 'B': return 0xB;
            case 'c':
            case 'C': return 0xC;
            case 'd':
            case 'D': return 0xD;
            case 'e':
            case 'E': return 0xE;
            case 'f':
            case 'F': return 0xF;
            default: throw new InvalidHexString("Illegal hex char: " + ch);
        }
    }

    /**
     * Thrown when the hex string is not valid.
     */
    public static class InvalidHexString extends IllegalArgumentException {
        public InvalidHexString(String s) {  super(s); }
    }

    @FunctionalInterface
    public interface ByteBufferAllocator { ByteBuffer allocate(int size); }

}
