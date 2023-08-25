package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
}
