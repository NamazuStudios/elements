package com.namazustudios.socialengine.rt.id;

/**
 * Interface for all types which have a compound ID.
 * 
 * @param <CompoundIdT>
 */
interface HasCompoundId<CompoundIdT extends Comparable<CompoundIdT>> extends Comparable<HasCompoundId<CompoundIdT>>  {

    /**
     * Gets the compound ID instance.
     * 
     * @return this instance's ID
     */
    CompoundIdT getId();

    /**
     * Compares this instance with the other instance.
     * 
     * @param o the instance
     * @return {@see {@link Comparable#compareTo(Object)}}
     */
    @Override
    default int compareTo(final HasCompoundId<CompoundIdT> o) {
        return getId().compareTo(o.getId());
    }

}
