import java.util.HashMap;
import java.util.Map;

public final class HateSpeechFilter {

    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean isEnd;
    }

    private static final Node ROOT = new Node();

    private HateSpeechFilter() {}

    /**
     * Add a banned word to the Trie.
     * The caller supplies all banned words. They are not provided by this class.
     */
    public static void addWord(String word) {
        if (word == null || word.isEmpty()) return;

        String w = word.toUpperCase();
        Node current = ROOT;

        for (char c : w.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
        }
        current.isEnd = true;
    }

    /**
     * Returns true if input contains any banned substring.
     */
    public static boolean containsBannedWord(String input) {
        if (input == null || input.isEmpty()) return false;

        String normalized = input.toUpperCase();

        for (int i = 0; i < normalized.length(); i++) {
            if (checkFromPosition(normalized, i)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkFromPosition(String input, int index) {
        Node current = ROOT;

        for (int i = index; i < input.length(); i++) {
            char c = input.charAt(i);
            current = current.children.get(c);
            if (current == null) {
                return false;
            }
            if (current.isEnd) {
                return true;
            }
        }

        return false;
    }

}
