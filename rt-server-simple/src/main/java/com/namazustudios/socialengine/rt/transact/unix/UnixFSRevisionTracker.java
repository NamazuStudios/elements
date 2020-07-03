package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;
import javolution.io.Struct;
import sun.reflect.generics.tree.Tree;

import javax.inject.Inject;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Serializes the actual update of the {@link Revision<?>} of the total database. This ensures that each sequentially
 * provided {@link Revision<?>} is committed and updated in order. This relies on the {@link UnixFSRevisionPool} to
 * issue new {@link Revision<?>} instances and ensures they are committed order. Additionally, this further ensures that
 * the {@link Revision<?>}s are committed in the same order in which they are issued.
 *
 * This approach does have some performance implications, specifically it forces that each revision be applied in order
 * therefore, threads attempting to commit may have to wait for others to complete. However, the implementation is
 * designed to be a lightweight approach, minimizing the chances that there is contention.
 */
public class UnixFSRevisionTracker {


}
