package dev.getelements.elements.service.blockchain.invoke.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import dev.getelements.elements.exception.InternalException;
import org.onflow.sdk.FlowScriptResponse;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

class FlowCallArgument {

    private static final ObjectMapper mapper = new ObjectMapper();

    private String type;

    private Object value;

    public static FlowCallArgument fromFlowScriptResponse(final FlowScriptResponse flowScriptResponse) {
        try {
            return mapper.readValue(flowScriptResponse.getStringValue(), FlowCallArgument.class);
        } catch (JsonProcessingException e) {
            throw new InternalException("Bad flow response.", e);
        }
    }

    public FlowCallArgument() {}

    public FlowCallArgument(final String type, final Object value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ByteString asByteString() {
        try {
            final var string = mapper.writeValueAsString(this);
            return ByteString.copyFrom(string, UTF_8);
        } catch (JsonProcessingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowCallArgument that = (FlowCallArgument) o;
        return Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "FlowArgument{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }

}
