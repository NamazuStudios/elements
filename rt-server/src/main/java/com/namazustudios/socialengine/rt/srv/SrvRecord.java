//package com.namazustudios.socialengine.rt.srv;
//
//import com.google.common.net.HostAndPort;
//import com.spotify.dns.LookupResult;
//
//import java.util.Objects;
//
///**
// * By convention, the SrvRecord advertises the port, but we do not utilize it.
// */
//public class SrvRecord {
//
//    final private String host;
//    final private int port;
//    private int priority;
//    private int weight;
//    private long ttl;
//
//    public SrvRecord(
//            final String host,
//            final int port,
//            final int priority,
//            final int weight,
//            final long ttl
//    ) {
//        this.host = host;
//        this.port = port;
//        this.priority = priority;
//        this.weight = weight;
//        this.ttl = ttl;
//    }
//
//    public static SrvRecord create(
//            final String host,
//            final int port,
//            final int priority,
//            final int weight,
//            final long ttl
//    ) {
//        return new SrvRecord(host, port, priority, weight, ttl);
//    }
//
//    public static SrvRecord createFromLookupResult(final LookupResult lookupResult) {
//        return new SrvRecord(
//                lookupResult.host(),
//                lookupResult.port(),
//                lookupResult.priority(),
//                lookupResult.weight(),
//                lookupResult.ttl()
//        );
//    }
//
//    /**
//     * If a difference is detected, performs an in-place update of the SrvRecord from the given lookupResult.
//     * Otherwise, no mutations will occur.
//     *
//     * @param lookupResult
//     * @return true if an update occurred, false if no update was necessary
//     */
//    public boolean updateFromLookupResultIfNecessary(final LookupResult lookupResult) {
//        if (priority != lookupResult.priority() ||
//                weight != lookupResult.weight() ||
//                ttl != lookupResult.ttl()) {
//            priority = lookupResult.priority();
//            weight = lookupResult.weight();
//            ttl = lookupResult.ttl();
//
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
//
//    public String getHost() {
//        return host;
//    }
//
//
//    public int getPort() {
//        return port;
//    }
//
//    public int getPriority() {
//        return priority;
//    }
//
//    public void setPriority(int priority) {
//        this.priority = priority;
//    }
//
//    public int getWeight() {
//        return weight;
//    }
//
//    public void setWeight(int weight) {
//        this.weight = weight;
//    }
//
//    public long getTtl() {
//        return ttl;
//    }
//
//    public void setTtl(long ttl) {
//        this.ttl = ttl;
//    }
//
//    public HostAndPort getHostAndPort() {
//        return HostAndPort.fromParts(getHost(), getPort());
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        SrvRecord srvRecord = (SrvRecord) o;
//        return getPort() == srvRecord.getPort() &&
//                getPriority() == srvRecord.getPriority() &&
//                getWeight() == srvRecord.getWeight() &&
//                getTtl() == srvRecord.getTtl() &&
//                Objects.equals(getHost(), srvRecord.getHost());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getHost(), getPort(), getPriority(), getWeight(), getTtl());
//    }
//
//    @Override
//    public String toString() {
//        return "SrvRecord{" +
//                "host='" + host + '\'' +
//                ", port=" + port +
//                ", priority=" + priority +
//                ", weight=" + weight +
//                ", ttl=" + ttl +
//                '}';
//    }
//}
