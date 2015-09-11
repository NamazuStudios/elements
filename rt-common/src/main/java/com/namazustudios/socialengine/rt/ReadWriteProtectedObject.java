package com.namazustudios.socialengine.rt;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;

/**
 * Created by patricktwohig on 9/5/15.
 */
public interface ReadWriteProtectedObject<ProtectedT> {

    /**
     * Exposes the underlying map for reading.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT read(CriticalSection<ReturnT, ProtectedT> criticalSection);

    /**
     * Exposes the underlying map for writing.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT write(CriticalSection<ReturnT, ProtectedT> criticalSection);

    /**
     * Gets a {@link com.namazustudios.socialengine.rt.ReadWriteProtectedObject.Monitor}
     * for reading the protected object.
     *
     * @return the {@link com.namazustudios.socialengine.rt.ReadWriteProtectedObject.Monitor} instance
     */
    Monitor<ProtectedT> read();

    /**
     * Gets a {@link com.namazustudios.socialengine.rt.ReadWriteProtectedObject.Monitor}
     * for writing the protected object.
     *
     * @return the {@link com.namazustudios.socialengine.rt.ReadWriteProtectedObject.Monitor} instance
     */
    Monitor<ProtectedT> write();

    /**
     * Simple callback for manipulating the underlying collection.
     *
     * @param <ReturnT> the return type.
     */
    interface CriticalSection<ReturnT, ProtectedT> {

        /**
         * Called to perform the critical operation.
         *
         * @param protectedObject the {@link ProtectedT}
         *
         * @return the custom return type
         */
        ReturnT perform(ProtectedT protectedObject);

    }

    /**
     * Used to guard the read or write operation, may be used in a try with resources block.
     *
     * @param <ProtectedT>
     */
    interface Monitor<ProtectedT> extends AutoCloseable {

        /**
         * Gets the protected object.
         *
         * @return the protected object.
         * @throws {@link IllegalStateException} if this object has been closed.
         */
        ProtectedT get();

        /**
         * Releseases any locks.
         */
        @Override
        void close();

    }

}
