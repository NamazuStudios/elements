package com.namazustudios.socialengine.doclet;

/**
 * A standard type to expose.
 */
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

}
