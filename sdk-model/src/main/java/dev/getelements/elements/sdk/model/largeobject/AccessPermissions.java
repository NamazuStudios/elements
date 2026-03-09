package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Defines read, write, and delete access permissions for a large object, each specifying
 * the set of subjects permitted to perform that operation.
 */
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

    /**
     * Returns the subjects permitted to read this object.
     *
     * @return the read subjects
     */
    public Subjects getRead() {
        return read;
    }

    /**
     * Sets the subjects permitted to read this object.
     *
     * @param read the read subjects
     */
    public void setRead(Subjects read) {
        this.read = read;
    }

    /**
     * Returns the subjects permitted to write to this object.
     *
     * @return the write subjects
     */
    public Subjects getWrite() {
        return write;
    }

    /**
     * Sets the subjects permitted to write to this object.
     *
     * @param write the write subjects
     */
    public void setWrite(Subjects write) {
        this.write = write;
    }

    /**
     * Returns the subjects permitted to delete this object.
     *
     * @return the delete subjects
     */
    public Subjects getDelete() {
        return delete;
    }

    /**
     * Sets the subjects permitted to delete this object.
     *
     * @param delete the delete subjects
     */
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
