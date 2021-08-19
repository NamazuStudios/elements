package com.namazustudios.socialengine.testsources;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.List;
import java.util.Map;

/**
 * A test class.
 *
 * @author ptwohig
 * @author sneakypete
 */
@Expose({
    @ModuleDefinition("test.javadoc.foo"),
    @ModuleDefinition("test.javadoc.bar"),
    @ModuleDefinition(value = "test.javadoc.deprecated", deprecated = @DeprecationDefinition(deprecated = true))
})
public interface ExposedTestJavadoc {

    /**
     * The Answer.
     */
    int FORTY_TWO = 42;

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
    default ExposedTestJavadoc getThis() {
        return this;
    }

}
