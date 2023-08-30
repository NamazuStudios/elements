package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class AccessPermissions {

    @Valid
    @NotNull
    @ApiModelProperty("Subjects allowed to read")
    private Subjects read;

    @Valid
    @NotNull
    @ApiModelProperty("Subjects allowed to write")
    private Subjects write;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessPermissions that = (AccessPermissions) o;
        return Objects.equals(read, that.read) && Objects.equals(write, that.write);
    }

    @Override
    public int hashCode() {
        return Objects.hash(read, write);
    }
}
