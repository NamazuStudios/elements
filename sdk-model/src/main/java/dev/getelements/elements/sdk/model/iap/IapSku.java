package dev.getelements.elements.sdk.model.iap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Defines the digital goods issued when a specific IAP product is purchased.")
public record IapSku(

        @Schema(description = "The database id of this IAP SKU.")
        String id,

        @NotNull
        @Schema(description = "The IAP provider schema in reverse-dns notation, e.g. com.apple.appstore.")
        String schema,

        @NotNull
        @Schema(description = "The product id as defined in the IAP provider's catalog.")
        String productId,

        @NotNull
        @NotEmpty
        @Schema(description = "The list of rewards issued when this SKU is purchased.")
        List<IapSkuReward> rewards

) {

    /** Returns a copy of this SKU with the given database id set. */
    public IapSku withId(final String id) {
        return new IapSku(id, schema, productId, rewards);
    }

}
