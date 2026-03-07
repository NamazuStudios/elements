package dev.getelements.elements.sdk.model.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Defines the digital goods issued when a specific product is purchased.")
public record ProductSku(

        @Schema(description = "The database id of this Product SKU.")
        String id,

        @NotNull
        @Schema(description = "The purchase provider schema in reverse-dns notation, e.g. com.apple.appstore.")
        String schema,

        @NotNull
        @Schema(description = "The product id as defined in the purchase provider's catalog.")
        String productId,

        @NotNull
        @NotEmpty
        @Schema(description = "The list of rewards issued when this SKU is purchased.")
        List<ProductSkuReward> rewards

) {

    /** Returns a copy of this SKU with the given database id set. */
    public ProductSku withId(final String id) {
        return new ProductSku(id, schema, productId, rewards);
    }

}
