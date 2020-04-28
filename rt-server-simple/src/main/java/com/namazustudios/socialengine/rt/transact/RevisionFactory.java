package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSObjectHeader;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSPathIndex;

import java.nio.file.Path;
import java.util.Optional;

public interface RevisionFactory {

    /**
     * Creates a {@link Revision<T>} with the supplied revision ID and value.  This parses the revision ID, as specified
     * by the {@link Revision#getUniqueIdentifier()}.  The resulting {@link Revision<T>} will have an
     * {@link Optional<T>} associated with it which has the specified value.
     *
     * @param at a string indicating the unique revision ID.
     * @param value the value to associate with the {@link Revision<T>}
     * @param <T>
     * @return a new {@link Revision<T>} instance
     */
    default <T> Revision<T> create(String at, T value) {
        return createOptional(at, Optional.of(value));
    }

    /**
     * Creates a {@link Revision<T>} with the supplied revision ID and value.  This parses the revision ID, as specified
     * by the {@link Revision#getUniqueIdentifier()}.  The resulting {@link Revision<T>} will have an
     * {@link Optional<T>} associated with it which has the specified vaue.
     *
     * @param at
     * @param value
     * @param <T>
     * @return a new {@link Revision<T>} instance
     */
    default <T> Revision<T> create(Revision<?> at, T value) {
        return createOptional(at, Optional.of(value));
    }

    /**
     * Creates a {@link Revision<T>} with the supplied revision ID and value.  This parses the revision ID, as specified
     * by the {@link Revision#getUniqueIdentifier()}.  The resulting {@link Revision<T>} will delegate functionality to
     * the supplied {@link Optional<T>}
     *
     * @param at a string indicating the unique revision ID.
     * @param optionalValue the {@link Optional<T>} to assocaite with the revision.s
     * @param <T>
     * @return a new {@link Revision<T>} instance
     */
    <T> Revision<T> createOptional(String at, Optional<T> optionalValue);

    /**
     * Creates a {@link Revision<T>} with the supplied revision ID and value.  This parses the revision ID, as specified
     * by the {@link Revision#getUniqueIdentifier()}.  The resulting {@link Revision<T>} will delegate functionality to
     * the supplied {@link Optional<T>}
     *
     * @param at the revision at wich to create the new revision
     * @param optionalValue the {@link Optional<T>} to assocaite with the revision.s
     * @param <T>
     * @return a new {@link Revision<T>} instance
     */
    default <T> Revision<T> createOptional(Revision<?> at, Optional<T> optionalValue) {
        return createOptional(at.getUniqueIdentifier(), optionalValue);
    }

}
