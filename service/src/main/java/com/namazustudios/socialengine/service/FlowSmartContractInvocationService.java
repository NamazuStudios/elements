package com.namazustudios.socialengine.service;

public interface FlowSmartContractInvocationService extends SmartContractInvocationService<FlowSmartContractInvocationService.Invoker> {

    interface Invoker {

        Object send();

        Object call();

    }

}
