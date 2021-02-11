package com.namazustudios.socialengine.rt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class HostList {

    private final List<String> hosts = new ArrayList<>();

    public HostList with(final String hosts) {

        requireNonNull(hosts, "hosts");

        Stream.of(hosts.trim().split("[\\s,;]+"))
              .map(host -> host.trim())
              .filter(host -> !host.isBlank())
              .forEach(this.hosts::add);

        return this;

    }

    public Optional<List<String>> get() {
        final var hosts = unmodifiableList(this.hosts);
        return hosts.isEmpty() ? Optional.empty() : Optional.of(hosts);
    }

}
