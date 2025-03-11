package dev.getelements.elements.rt.remote;

import java.util.Objects;

/**
 * Gets host information for an instance.  Objects implementing this interface must implement {@link Object#hashCode()}
 * and {@link Object#equals(Object)} such that this can be used as a hash map key.
 */
public interface InstanceHostInfo {

    /**
     * Gets the address for the invoker service
     *
     * @return the invoker address
     */
    String getConnectAddress();

    /**
     * Implements the logic for {@link Object#hashCode()}.
     *
     * @param nfo the {@link InstanceHostInfo} for which to compute the hash code
     * @return
     */
    static int hashCode(final InstanceHostInfo nfo) {
        return Objects.hashCode(nfo.getConnectAddress());
    }

    /**
     * Tests an instance of {@link InstanceHostInfo} against an arbitrary {@link Object}
     *
     * @param a the {@link InstanceHostInfo} to test
     * @param b the {@link Object} to test
     * @return true if equal, false otherwise
     */
    static boolean equals(final InstanceHostInfo a, final Object b) {
        return (b instanceof InstanceHostInfo) && equals(a, (InstanceHostInfo)b);
    }

    /**
     * Tests an instance of {@link InstanceHostInfo} against another instance of {@link InstanceHostInfo}
     *
     * @param a the {@link InstanceHostInfo} to test
     * @param b the {@link InstanceHostInfo} to test
     * @return true if equal, false otherwise
     */
    static boolean equals(final InstanceHostInfo a, final InstanceHostInfo b) {
        return Objects.equals(a.getConnectAddress(), b.getConnectAddress());
    }

}
