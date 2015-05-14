package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/13/15.
 */
public interface Index {

    /**
     * Adds an object to the index.  The object must be a type which has the @
     *
     * @param model
     */
    void add(Object model);

    /**
     * Deletes the object from the index.
     *
     * @param model
     */
    void delete(Object model);

    /**
     * Searches for the given type.
     *
     * @param query
     * @param <T>
     * @return
     */
    <T> Iterable<T> search(String query);

}
