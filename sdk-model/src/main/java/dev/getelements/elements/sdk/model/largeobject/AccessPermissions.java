package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class AccessPermissions {

    @Valid
    @NotNull
    @Schema(description = "Subjects allowed to read")
    private Subjects read;

    @Valid
    @NotNull
    @Schema(description = "Subjects allowed to write")
    private Subjects write;

    @Valid
    @NotNull
    @Schema(description = "Subjects allowed to delete")
    private Subjects delete;

    public Subjects getRead() {
        return read;
    }

    public void setRead(Subjects read) {
        this.read = read;
    }

    public Subjects getWrite() {
        return write;
    }

    public void setWrite(Subjects write) {
        this.write = write;
    }

    public Subjects getDelete() {
        return delete;
    }

    public void setDelete(Subjects delete) {
        this.delete = delete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessPermissions that = (AccessPermissions) o;
        return Objects.equals(read, that.read) && Objects.equals(write, that.write) && Objects.equals(delete, that.delete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(read, write, delete);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessPermissions{");
        sb.append("read=").append(read);
        sb.append(", write=").append(write);
        sb.append(", delete=").append(delete);
        sb.append('}');
        return sb.toString();
    }

}
