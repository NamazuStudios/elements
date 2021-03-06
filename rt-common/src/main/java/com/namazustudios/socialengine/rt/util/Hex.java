package com.namazustudios.socialengine.rt.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * A set of utilities for encoding and decoding Hexadecimal strings.
 */
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

    private static final char[] HEX_CHARS_UPPER = "0123456789ABCDEF".toCharArray();

    private static final char[] HEX_CHARS_LOWER = "0123456789abcdef".toCharArray();

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @return the {@link String} representing the hex string
     */
    public static String encode(final byte[] raw) {
        return encode(raw, Case.LOWER);
    }

    /**
     * Encodes hex to to lower-case hex encoding.
     *
     * @param raw the raw byte value
     * @param c the {@link Case}
     * @return the {@link String} representing the hex string
     */
    public static String encode(final byte[] raw, final Case c) {
        switch (c == null ? Case.LOWER : c) {
            case LOWER: return encode(raw, HEX_CHARS_LOWER);
            case UPPER: return encode(raw, HEX_CHARS_UPPER);
            default:    throw new IllegalArgumentException("Undefined case " + c);
        }
    }

    private static String encode(final byte[] raw, final char[] output) {

        final ByteBuffer bytes = ByteBuffer.wrap(raw);
        final CharBuffer chars = CharBuffer.allocate(raw.length * 2);

        while(bytes.hasRemaining()) {
            int value = bytes.get() & 0xFF;
            chars.put(output[value >>> 4]);
            chars.put(output[value & 0xF]);
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

        if ((hex.length() % 2) == 1) throw new InvalidHexString("Invalid hex string " + hex);

        final int length = hex.length();
        final CharBuffer chars = CharBuffer.wrap(hex);
        final ByteBuffer bytes = ByteBuffer.allocate(length / 2);

        while (chars.hasRemaining()) {
            final int upper = decode(chars.get()) << 4;
            final int lower = decode(chars.get()) & 0xFF;
            bytes.put((byte)(upper | lower));
        }

        return bytes.array();

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

}
