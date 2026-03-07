package dev.getelements.elements.sdk.model.iap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A single reward issued as part of an IAP SKU purchase.")
public record IapSkuReward(

        @NotNull
        @Schema(description = "The id or name of the item to issue.")
        String itemId,

        @Schema(description = "The quantity to issue. Null for DISTINCT items; positive integer for FUNGIBLE items.")
        Integer quantity

) {}
