package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InstanceMetadata

class InstanceMetadataEnvelope(override val payload: InstanceMetadata) : Envelope<InstanceMetadata> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION

}