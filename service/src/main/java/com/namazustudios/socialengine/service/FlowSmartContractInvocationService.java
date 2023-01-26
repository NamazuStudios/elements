package com.namazustudios.socialengine.service;

public interface FlowSmartContractInvocationService {

    SmartContractInvocationResolution<Invoker> resolve();

    interface Invoker {}

}
