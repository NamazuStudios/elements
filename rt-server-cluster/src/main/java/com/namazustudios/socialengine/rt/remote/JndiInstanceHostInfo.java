package com.namazustudios.socialengine.rt.remote;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.String.format;

public class JndiInstanceHostInfo implements InstanceHostInfo, Comparable<JndiInstanceHostInfo> {

    private final int priority;

    private final int weight;

    private final int port;

    private final String target;

    private final String connectAddress;

    public JndiInstanceHostInfo(final String protocol, final String record) {

        final var attrs = record.split("\\s+");
        if (attrs.length != 4) throw new IllegalArgumentException("Invalid SRV Record:" + record);

        priority = Integer.parseInt(attrs[0]);
        weight = Integer.parseInt(attrs[1]);
        port = Integer.parseInt(attrs[2]);
        target = attrs[3];

        connectAddress = format("%s://%s:%s",
            protocol.replaceFirst("_", ""),
            target.endsWith(".") ? target.substring(0, target.length() - 1) : target,
            port);

    }

    @Override
    public String toString() {
        return format("SRV (JNDI) Record %s:%d %d %d", target, port, priority, weight);
    }

    @Override
    public String getConnectAddress() {
        return connectAddress;
    }

    @Override
    public int compareTo(final JndiInstanceHostInfo o) {
        return !target.equals(o.target) ? target.compareTo(o.target) :
               port != o.port           ? port - o.port              :
               priority != o.priority   ? priority - o.priority
                                        : weight - o.weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JndiInstanceHostInfo that = (JndiInstanceHostInfo) o;
        return Objects.equals(getConnectAddress(), that.getConnectAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConnectAddress());
    }

    public static SortedSet<JndiInstanceHostInfo> parse(final String protocol, final Attribute attribute) {
        try {

            final var enumeration = attribute.getAll();

            try {

                final SortedSet<JndiInstanceHostInfo> result = new TreeSet<>();

                while (enumeration.hasMore()) {
                    final var record = enumeration.next().toString();
                    result.add(new JndiInstanceHostInfo(protocol, record));
                }

                return result;

            } finally {
                enumeration.close();
            }

        } catch (NamingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
