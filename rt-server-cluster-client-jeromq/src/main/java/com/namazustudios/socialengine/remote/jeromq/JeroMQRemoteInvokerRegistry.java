//package com.namazustudios.socialengine.remote.jeromq;
//
//import com.namazustudios.socialengine.rt.id.NodeId;
//import com.namazustudios.socialengine.rt.jeromq.RouteRepresentationUtil;
//import com.namazustudios.socialengine.rt.remote.ConnectionService;
//import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
//import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.stream.Collectors;
//
//public class JeroMQRemoteInvokerRegistry implements RemoteInvokerRegistry {
//
//    private final AtomicReference<Map<NodeId, RemoteInvoker>> atomicInstanceRemoteInvokersReference = new AtomicReference<>(new HashMap<>());
//    private final AtomicReference<Map<NodeId, RemoteInvoker>> atomicApplicationRemoteInvokersReference = new AtomicReference<>(new HashMap<>());
//
//    private Provider<RemoteInvoker> remoteInvokerProvider;
//
//    private ConnectionService connectionService;
////
////    @Override
////    public void onInstancesConnected(final Set<UUID> instanceUuids) {
////        final Set<NodeId> instanceNodeIds = instanceUuids
////            .stream()
////            .map(instanceUuid -> new NodeId(instanceUuid, null))
////            .collect(Collectors.toSet());
////
////        synchronized(atomicInstanceRemoteInvokersReference) {
////            final Map<NodeId, RemoteInvoker> instanceRemoteInvokers = atomicInstanceRemoteInvokersReference.get();
////
////            instanceNodeIds.forEach(instanceNodeId -> {
////                // only stand up/connect a remote invoker if necessary in case we accidentally are told an instance appeared twice (should not happen by contract)
////                instanceRemoteInvokers.computeIfAbsent(instanceNodeId, ini -> {
////                    final RemoteInvoker instanceRemoteInvoker = getRemoteInvokerProvider().get();
////                    final String connectAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(instanceNodeId);
////                    instanceRemoteInvoker.start(connectAddress);
////                    return instanceRemoteInvoker;
////                });
////            });
////        }
////    }
////
////    @Override
////    public void onInstancesDisconnected(final Set<UUID> instanceUuids) {
////        final Set<NodeId> instanceNodeIds = instanceUuids
////            .stream()
////            .map(instanceUuid -> new NodeId(instanceUuid, null))
////            .collect(Collectors.toSet());
////
////        synchronized(atomicInstanceRemoteInvokersReference) {
////            final Map<NodeId, RemoteInvoker> remoteInvokers = atomicInstanceRemoteInvokersReference.get();
////            instanceNodeIds.forEach(instanceNodeId -> {
////                final RemoteInvoker removedInstanceRemoteInvoker = remoteInvokers.remove(instanceNodeId);
////
////                if (removedInstanceRemoteInvoker != null) {
////                    removedInstanceRemoteInvoker.stop();
////                }
////            });
////
////        }
////    }
//
//    public void onApplicationsAppeared(final UUID instanceUuid, final Set<UUID> applicationUuids) {
//        final List<NodeId> applicationNodeIds = applicationUuids
//                .stream()
//                .map(applicationUuid -> new NodeId(instanceUuid, applicationUuid))
//                .collect(Collectors.toList());
//
//        synchronized (atomicApplicationRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> applicationRemoteInvokers = atomicApplicationRemoteInvokersReference.get();
//
//            applicationNodeIds.forEach(applicationNodeId -> {
//                // only stand up/connect a remote invoker if necessary in case we accidentally are told an application appeared twice (should not happen by contract)
//                applicationRemoteInvokers.computeIfAbsent(applicationNodeId, ani -> {
//                    final RemoteInvoker applicationRemoteInvoker = getRemoteInvokerProvider().get();
//                    final String connectAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(applicationNodeId);
//                    applicationRemoteInvoker.start(connectAddress);
//                    return applicationRemoteInvoker;
//                });
//            });
//        }
//    }
//
//    public void onApplicationsDisappeared(final UUID instanceUuid, final Set<UUID> applicationUuids) {
//        final List<NodeId> applicationNodeIds = applicationUuids
//                .stream()
//                .map(applicationUuid -> new NodeId(instanceUuid, applicationUuid))
//                .collect(Collectors.toList());
//
//        synchronized (atomicApplicationRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> applicationRemoteInvokers = atomicApplicationRemoteInvokersReference.get();
//
//            final List<RemoteInvoker> removedApplicationRemoteInvokers = applicationNodeIds
//                    .stream()
//                    .map(applicationNodeId -> applicationRemoteInvokers.remove(applicationNodeId))
//                    .filter(removedApplicationRemoteInvoker -> removedApplicationRemoteInvoker != null)
//                    .collect(Collectors.toList());
//
//            removedApplicationRemoteInvokers.forEach(removedApplicationRemoteInvoker -> removedApplicationRemoteInvoker.stop());
//        }
//    }
//
//    @Override
//    public RemoteInvoker getAnyInstanceRemoteInvoker() {
//        synchronized(atomicInstanceRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> instanceRemoteInvokers = atomicInstanceRemoteInvokersReference.get();
//            final Optional<RemoteInvoker> optionalRemoteInvoker = instanceRemoteInvokers
//                    .values()
//                    .stream()
//                    .skip((int) (instanceRemoteInvokers.size() * Math.random()))
//                    .findFirst();
//            if (optionalRemoteInvoker != null) {
//                return optionalRemoteInvoker.get();
//            }
//            else {
//                return null;
//            }
//        }
//    }
//
//    @Override
//    public RemoteInvoker getAnyApplicationRemoteInvoker() {
//        synchronized(atomicApplicationRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> applicationRemoteInvokers = atomicApplicationRemoteInvokersReference.get();
//            final Optional<RemoteInvoker> optionalRemoteInvoker = applicationRemoteInvokers
//                    .values()
//                    .stream()
//                    .skip((int) (applicationRemoteInvokers.size() * Math.random()))
//                    .findFirst();
//            if (optionalRemoteInvoker != null) {
//                return optionalRemoteInvoker.get();
//            }
//            else {
//                return null;
//            }
//        }
//    }
//
//    @Override
//    public RemoteInvoker getRemoteInvoker(NodeId nodeId) {
//        RemoteInvoker remoteInvoker = getApplicationRemoteInvoker(nodeId);
//
//        if (remoteInvoker == null) {
//            remoteInvoker = getInstanceRemoteInvoker(nodeId);
//        }
//
//        return remoteInvoker;
//    }
//
//    @Override
//    public RemoteInvoker getInstanceRemoteInvoker(NodeId nodeId) {
//        synchronized(atomicInstanceRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> instanceRemoteInvokers = atomicInstanceRemoteInvokersReference.get();
//            return instanceRemoteInvokers.get(nodeId);
//        }
//    }
//
//    @Override
//    public RemoteInvoker getApplicationRemoteInvoker(NodeId nodeId) {
//        synchronized(atomicApplicationRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> applicationRemoteInvokers = atomicApplicationRemoteInvokersReference.get();
//            return applicationRemoteInvokers.get(nodeId);
//        }
//    }
//
//    @Override
//    public Set<RemoteInvoker> getAllInstanceRemoteInvokers() {
//        synchronized(atomicInstanceRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> instanceRemoteInvokers = atomicInstanceRemoteInvokersReference.get();
//            return instanceRemoteInvokers.values().stream().collect(Collectors.toSet());
//        }
//    }
//
//    @Override
//    public Set<RemoteInvoker> getAllApplicationRemoteInvokers() {
//        synchronized(atomicApplicationRemoteInvokersReference) {
//            final Map<NodeId, RemoteInvoker> applicationRemoteInvokers = atomicApplicationRemoteInvokersReference.get();
//            return applicationRemoteInvokers.values().stream().collect(Collectors.toSet());
//        }
//    }
//
//    public Provider<RemoteInvoker> getRemoteInvokerProvider() {
//        return remoteInvokerProvider;
//    }
//
//    @Inject
//    public void setRemoteInvokerProvider(Provider<RemoteInvoker> remoteInvokerProvider) {
//        this.remoteInvokerProvider = remoteInvokerProvider;
//    }
//
//    public ConnectionService getConnectionService() {
//        return connectionService;
//    }
//
//    @Inject
//    public void setConnectionService(ConnectionService connectionService) {
//        this.connectionService = connectionService;
//    }
//}