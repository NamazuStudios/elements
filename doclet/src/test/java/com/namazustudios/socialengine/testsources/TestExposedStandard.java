package com.namazustudios.socialengine.testsources;

import com.namazustudios.socialengine.rt.annotation.Public;

/**
 * A standard type to expose.
 *
 * Try looking at {@link TestExposedStandard#getFoo()}
 *
 * {@see http://java.net}
 */
@Public
public class TestExposedStandard {

    private int foo;

    private String bar;

    /**
     * Gets the foo value.
     *
     * @return the foo value
     */
    public int getFoo() {
        return foo;
    }

    /**
     * Sets the foo value.
     *
     * @param foo the foo value
     */
    public void setFoo(int foo) {
        this.foo = foo;
    }

    /**
     * Gets the bar value.
     *
     * @return the bar value
     */
    public String getBar() {
        return bar;
    }

    /**
     * Sets the bar value.
     *
     * @param bar the bar value
     */
    public void setBar(String bar) {
        this.bar = bar;
    }

    /**
     * Adds foo to the current value of foo.
     *
     * @param foo the value to add
     * @return the result of the addition
     */
    public int add(int foo) {
        return this.foo += foo;
    }

    /**
     * Subtracts foo from the current value of food.
     *
     * @param foo the value to subtract
     * @return the result of subtraction
     */
    public int sub(int foo) {
        return this.foo += foo;
    }

    /**
     * Appends bar to this bar's value.
     *
     * @param bar the bar value
     * @return the result of appending
     */
    public String append(final String bar) {
        return this.bar += bar;
    }

    /**
     * Lorem Ipsum. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore
     * et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea
     * commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
     * pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est
     * laborum.
     *
     */
    public void loremIpsum() {}

    /**
     * Uses a link.
     *
     * {@see http://google.com}
     *
     */
    public void testSee() {}

    public void undocumentedMethod() {}

}
