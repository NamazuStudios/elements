package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterators.limit;
import static com.google.common.collect.Lists.newArrayList;
import static com.namazustudios.socialengine.rt.Path.Util.*;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents Application Node metadata for a node on the network.
 */
public class ApplicationNodeMetadata implements Serializable {
    private UUID nodeUuid;

    private int resourceAllocation;

    private double loadAverage;

    public UUID getNodeUuid() {
        return nodeUuid;
    }

    public void setNodeUuid(UUID nodeUuid) {
        this.nodeUuid = nodeUuid;
    }

    public int getResourceAllocation() {
        return resourceAllocation;
    }

    public void setResourceAllocation(int resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }
}
