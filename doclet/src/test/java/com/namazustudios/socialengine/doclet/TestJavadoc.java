package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.List;
import java.util.Map;

/**
 * A test class.
 *
 * @author ptwohig
 * @author sneakypete
 */
@Expose({
    @ExposedModuleDefinition("test.javadoc.foo"),
    @ExposedModuleDefinition("test.javadoc.bar"),
    @ExposedModuleDefinition(value = "test.javadoc.deprecated", deprecated = @DeprecationDefinition(deprecated = true))
})
public interface TestJavadoc {

    /**
     * A simple test method.
     */
    void foo();

    /**
     * A simple test method with parameters.
     *
     * @param a parameter a
     * @param b parameter b
     */
    void foo(int a, int b);

    /**
     * A simple test method with return type.
     *
     * @param a parameter a
     * @param b parameter b
     *
     * @return the sum of a and b
     */
    int add(int a, int b);

    /**
     * A method which combines float x and y to an array representing a point.
     *
     * @param x
     * @param y
     * @return
     */
    float[] getArray(float x, float y);

    /**
     * Gets a mapping.
     */
    Map<String, String> getMapping();

    /**
     * Gets a list.
     * @return a list
     */
    List<String> getList();

    /**
     * Gets a complicated list.
     * @return a list
     */
    List<Map<String, Integer>> getComplicatedList();

    /**
     * Gets a complicated map.
     *
     * @return the complicated map
     */
    Map<String, List<Float>> getComplicatedMap();

    /**
     * Returns this object.
     */
    default TestJavadoc getThis() {
        return this;
    }

}
