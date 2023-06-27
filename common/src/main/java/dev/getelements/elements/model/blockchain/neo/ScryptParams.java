package dev.getelements.elements.model.blockchain.neo;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ScryptParams {

    @ApiModelProperty("n -> cost")
    private int n;

    @ApiModelProperty("r -> blocksize")
    private int r;

    @ApiModelProperty("p -> parallel")
    private int p;

    public ScryptParams() {
    }

    public ScryptParams(int n, int r, int p) {
        this.n = n;
        this.r = r;
        this.p = p;
    }

    public int getN() {
        return n;
    }

    public int getR() {
        return r;
    }

    public int getP() {
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScryptParams)) return false;
        ScryptParams that = (ScryptParams) o;
        return getN() == that.getN() &&
                getR() == that.getR() &&
                getP() == that.getP();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getN(), getR(), getP());
    }

    @Override
    public String toString() {
        return "ScryptParams{" +
                "n=" + n +
                ", r=" + r +
                ", p=" + p +
                '}';
    }
}
