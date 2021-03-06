package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InvalidNodeIdException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZMsg;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.NO_SUCH_INSTANCE;

public class JeroMQUnroutableInstanceException extends JeroMQControlException {

    private final InstanceId instanceId;

    public JeroMQUnroutableInstanceException(final InstanceId instanceId) {
        super(NO_SUCH_INSTANCE);
        this.instanceId = instanceId;
    }

    public JeroMQUnroutableInstanceException(final ZMsg response) {
        super(NO_SUCH_INSTANCE);
        this.instanceId = parseInstanceId(response);
    }

    private static InstanceId parseInstanceId(final ZMsg response) {

        if (response.isEmpty()) return null;

        try {
            final byte[] data = response.removeFirst().getData();
            return new InstanceId(data);
        } catch (InvalidNodeIdException ex) {
            return null;
        }

    }

    /**
     * Gets the {@link InstanceId} that was returned.
     *
     * @return the {@link NodeId}
     */
    public InstanceId getInstanceId() {
        return instanceId;
    }

}
