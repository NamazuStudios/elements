package dev.getelements.elements.model.schema;

import dev.getelements.elements.model.schema.json.JsonSchema;
import dev.getelements.elements.model.schema.layout.EditorLayout;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description =
        "Defines an editor schema which contains a JSON Schema, layout, and initial data for the editor."
)
public class EditorSchema {

    @ApiModelProperty("The JSON Schema for the Data")
    private JsonSchema schema;

    @ApiModelProperty("The editor layout.")
    private List<EditorLayout> layout;

    @ApiModelProperty("The data itself.")
    private Object data;

    public JsonSchema getSchema() {
        return schema;
    }

    public void setSchema(JsonSchema schema) {
        this.schema = schema;
    }

    public List<EditorLayout> getLayout() {
        return layout;
    }

    public void setLayout(List<EditorLayout> layout) {
        this.layout = layout;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditorSchema that = (EditorSchema) o;
        return Objects.equals(getSchema(), that.getSchema()) && Objects.equals(getLayout(), that.getLayout()) && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSchema(), getLayout(), getData());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditorSchema{");
        sb.append("schema=").append(schema);
        sb.append(", layout=").append(layout);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }

}