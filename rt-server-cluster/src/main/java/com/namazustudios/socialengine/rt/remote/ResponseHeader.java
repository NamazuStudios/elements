package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.ErrorHandler;
import com.namazustudios.socialengine.rt.annotation.ResultHandler;
import javolution.io.Struct;

/**
 * Prepends a message response, providing scope and other information about the response.
 */
public class ResponseHeader extends Struct {

    /**
     * The uniquely assigned request identifier.  This further subdivides the invocation into multiple requests for each
     * of the unique ID of the particular invocation.  The request with the value of zero must be the initial request
     * containing the {@link Invocation} and subsequent values correspond to the index of the {@link ResultHandler}
     * annotated parameter.  Any response with an error may be routed to the {@link ErrorHandler} annotated parameter.
     *
     */
    public final Signed32 part = new Signed32();

    /**
     * Indicates the message type as specified by {@link MessageType}.
     */
    public final Enum32<MessageType> type = new Enum32<>(MessageType.values());

}
