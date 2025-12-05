package dev.getelements.elements.sdk.util;

public final class Metaphone {

    public static String metaphone(CharSequence input) {
        return metaphone(input, 4);
    }

    public static String metaphone(CharSequence input, int maxCodeLen) {
        if (input == null || input.length() == 0) {
            return "";
        }

        int n = input.length();
        StringBuilder cleaned = new StringBuilder(n);

        // keep only letters and uppercase them
        for (int i = 0; i < n; i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                cleaned.append(Character.toUpperCase(c));
            }
        }

        if (cleaned.length() == 0) {
            return "";
        }

        return metaphoneInternal(cleaned, maxCodeLen);
    }

    private static String metaphoneInternal(CharSequence input, int maxCodeLen) {
        int len = input.length();

        // initial transforms applied directly to CharSequence
        int start = 0;

        if (startsWith(input, "KN", "GN", "PN", "AE", "WR")) {
            start = 1;
        } else if (startsWith(input, "WH")) {
            // replace WH -> W
            StringBuilder tmp = new StringBuilder(len - 1);
            tmp.append('W').append(input, 2, len);
            input = tmp;
            len = input.length();
        } else if (input.charAt(0) == 'X') {
            // X at start -> S
            StringBuilder tmp = new StringBuilder(len);
            tmp.append('S').append(input, 1, len);
            input = tmp;
            len = input.length();
        }

        StringBuilder out = new StringBuilder(maxCodeLen);
        char prev = 0;

        for (int i = start; i < len && out.length() < maxCodeLen; ) {
            char c = input.charAt(i);
            char next = charAt(input, i + 1);
            char next2 = charAt(input, i + 2);
            char next3 = charAt(input, i + 3);

            // drop duplicate letters except C
            if (c == prev && c != 'C') {
                i++;
                continue;
            }

            switch (c) {

                case 'A': case 'E': case 'I': case 'O': case 'U':
                    if (i == start) {
                        append(out, c, maxCodeLen);
                    }
                    i++;
                    break;

                case 'B':
                    if (!(prev == 'M' && i == len - 1)) {
                        append(out, 'B', maxCodeLen);
                    }
                    i++;
                    break;

                case 'C':
                    if (next == 'I' && next2 == 'A') {
                        append(out, 'X', maxCodeLen);
                        i += 3;
                    } else if (next == 'H') {

                        // Greek hard CH rule
                        if (isGreekCH(input, i)) {
                            append(out, 'K', maxCodeLen);
                        } else if (prev == 'S') {
                            append(out, 'K', maxCodeLen);
                        } else {
                            append(out, 'X', maxCodeLen);
                        }
                        i += 2;

                    } else if (next == 'I' || next == 'E' || next == 'Y') {
                        append(out, 'S', maxCodeLen);
                        i += 2;
                    } else if (next == 'K') {
                        append(out, 'K', maxCodeLen);
                        i += 2;
                    } else {
                        append(out, 'K', maxCodeLen);
                        i++;
                    }
                    break;

                case 'D':

                    // Silent D in Dj… names (Django, Djokovic)
                    if (i == 0 && next == 'J') {
                        i++;
                        break;
                    }

                    if (next == 'G' && (next2 == 'E' || next2 == 'Y' || next2 == 'I')) {
                        append(out, 'J', maxCodeLen);
                        i += 3;
                    } else {
                        append(out, 'T', maxCodeLen);
                        i++;
                    }
                    break;

                case 'G':
                    if (next == 'H') {
                        if (i + 2 < len && !isVowel(next2)) {
                            i += 2;
                        } else {
                            i++;
                        }
                    } else if (next == 'N') {
                        if (i + 1 == len - 1 ||
                                (next2 == 'E' && next3 == 'D' && i + 3 == len - 1)) {
                            i++;
                        } else {
                            append(out, 'K', maxCodeLen);
                            i++;
                        }
                    } else if ((next == 'E' || next == 'I' || next == 'Y') && prev != 'G') {
                        append(out, 'J', maxCodeLen);
                        i++;
                    } else {
                        append(out, 'K', maxCodeLen);
                        i++;
                    }
                    break;

                case 'H':
                    if (isVowel(prev) && !isVowel(next)) {
                        i++;
                    } else {
                        append(out, 'H', maxCodeLen);
                        i++;
                    }
                    break;

                case 'F': case 'J': case 'L': case 'M':
                case 'N': case 'R':
                    append(out, c, maxCodeLen);
                    i++;
                    break;

                case 'K':
                    if (prev != 'C') append(out, 'K', maxCodeLen);
                    i++;
                    break;

                case 'P':
                    if (next == 'H') {
                        append(out, 'F', maxCodeLen);
                        i += 2;
                    } else {
                        append(out, 'P', maxCodeLen);
                        i++;
                    }
                    break;

                case 'Q':
                    append(out, 'K', maxCodeLen);
                    i++;
                    break;

                case 'S':
                    if (next == 'H') {
                        append(out, 'X', maxCodeLen);
                        i += 2;
                    } else if (next == 'I' && (next2 == 'O' || next2 == 'A')) {
                        append(out, 'X', maxCodeLen);
                        i += 3;
                    } else {
                        append(out, 'S', maxCodeLen);
                        i++;
                    }
                    break;

                case 'T':
                    if (next == 'I' && (next2 == 'O' || next2 == 'A')) {
                        append(out, 'X', maxCodeLen);
                        i += 3;
                    } else if (next == 'H') {
                        append(out, '0', maxCodeLen);
                        i += 2;
                    } else if (next == 'C' && next2 == 'H') {
                        i++;
                    } else {
                        append(out, 'T', maxCodeLen);
                        i++;
                    }
                    break;

                case 'V':
                    append(out, 'F', maxCodeLen);
                    i++;
                    break;

                case 'W':
                    if (isVowel(next)) {
                        append(out, 'W', maxCodeLen);
                    }
                    i++;
                    break;

                case 'X':
                    if (i == 0) {
                        append(out, 'S', maxCodeLen);
                    } else {
                        append(out, 'K', maxCodeLen);
                        append(out, 'S', maxCodeLen);
                    }
                    i++;
                    break;

                case 'Y':
                    if (isVowel(next)) {
                        append(out, 'Y', maxCodeLen);
                    }
                    i++;
                    break;

                case 'Z':
                    append(out, 'S', maxCodeLen);
                    i++;
                    break;

                default:
                    i++;
            }

            prev = c;
        }

        return out.toString();
    }

    private static boolean startsWith(CharSequence cs, String... prefixes) {
        for (String p : prefixes) {
            if (cs.length() >= p.length()) {
                boolean match = true;
                for (int i = 0; i < p.length(); i++) {
                    if (cs.charAt(i) != p.charAt(i)) {
                        match = false;
                        break;
                    }
                }
                if (match) return true;
            }
        }
        return false;
    }

    private static char charAt(CharSequence cs, int i) {
        return (i >= 0 && i < cs.length()) ? cs.charAt(i) : 0;
    }

    private static boolean isVowel(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U';
    }

    private static void append(StringBuilder out, char c, int max) {
        if (out.length() < max) out.append(c);
    }

    private static boolean isGreekCH(CharSequence cs, int index) {
        char c2 = charAt(cs, index + 2);
        return c2 == 'A' || c2 == 'O' || c2 == 'U' || c2 == 'R' || c2 == 'L';
    }
}
