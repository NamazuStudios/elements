package com.namazustudios.socialengine.appnode.provider;

import com.namazustudios.socialengine.rt.Node;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class DestinationIdSetProvider implements Provider<Set<String>> {

    private Provider<Set<Node>> nodeSetProvider;

    @Override
    public Set<String> get() {
        return getNodeSetProvider()
            .get()
            .stream()
            .map(node -> node.getId())
            .collect(toCollection(LinkedHashSet::new));
    }

    public Provider<Set<Node>> getNodeSetProvider() {
        return nodeSetProvider;
    }

    @Inject
    public void setNodeSetProvider(Provider<Set<Node>> nodeSetProvider) {
        this.nodeSetProvider = nodeSetProvider;
    }

}
