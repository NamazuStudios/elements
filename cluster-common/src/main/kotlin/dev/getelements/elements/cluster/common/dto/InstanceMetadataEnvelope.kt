package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InstanceMetadata

/**
 * Carries an [InstanceMetadata] snapshot describing a cluster instance (e.g. its address and identity) to
 * remote peers, used for cluster membership/discovery rather than as a response to any single invocation.
 *
 * @property payload the instance metadata
 */
class InstanceMetadataEnvelope(override val payload: InstanceMetadata) : Envelope<InstanceMetadata> {

    override val type: Envelope.Type = Envelope.Type.INSTANCE_METADATA

}