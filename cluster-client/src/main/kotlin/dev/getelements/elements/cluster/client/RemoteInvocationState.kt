package dev.getelements.elements.cluster.client

import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult

interface RemoteInvocationState {

    suspend fun onError(throwable: Throwable)

    suspend fun onInvocationError(error: InvocationErrorEnvelope)

    suspend fun onInvocationResult(result: InvocationResultEnvelope)

}
