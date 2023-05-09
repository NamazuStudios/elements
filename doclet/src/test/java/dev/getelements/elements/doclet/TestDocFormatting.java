package dev.getelements.elements.doclet;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

public class TestDocFormatting {

    private static final String SHORT = "Lorem ipsum dolor sit amet.";

    private static final String LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
            "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation " +
            "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
            "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private static final String LONG_WITH_HTML = "Lorem ipsum dolor sit <i>amet</i>, consectetur " +
            "<b>adipiscing</b> elit, sed do eiusmod tempor incididunt ut <href=\"google.com\">labore</href> et " +
            "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip " +
            "ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore " +
            "eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia " +
            "deserunt mollit anim id est laborum.";

    @DataProvider
    public static Object[][] getLengths() {
        return IntStream
            .range(4, 120)
            .mapToObj(i -> new Object[]{i})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getLengths")
    public void testLong(int cols) {
        DocFormatting.split(LONG, cols, "-- ");
    }

    @Test(dataProvider = "getLengths")
    public void testShort(int cols) {
        final var result = DocFormatting.split(SHORT, cols, "-- ");
    }

    @Test
    public void testLongWithHtml() {
        DocFormatting.split(LONG_WITH_HTML, 120, "-- ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExceptionLong() {
        DocFormatting.split(LONG, 3, "-- ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExceptionShort() {
        DocFormatting.split(SHORT, 3, "-- ");
    }

}
