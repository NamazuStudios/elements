package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/**
 * A freshly-generated set of one-time recovery codes, returned exactly once at successful enrollment
 * confirmation. Each code may be used exactly once, in place of a TOTP code, if the enrolled device is lost.
 * They are never retrievable again after this response -- only regenerable by re-enrolling.
 */
@Schema(description = "One-time recovery codes, shown only once, immediately after enrollment is confirmed.")
public class TotpRecoveryCodes {

    /** Creates a new instance. */
    public TotpRecoveryCodes() {}

    @Schema(description = "The recovery codes. Store these somewhere safe -- they are not shown again.")
    private List<String> codes;

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpRecoveryCodes that = (TotpRecoveryCodes) o;
        return Objects.equals(codes, that.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codes);
    }

    @Override
    public String toString() {
        return "TotpRecoveryCodes{...}";
    }

}
