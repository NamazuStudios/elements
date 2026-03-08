package dev.getelements.elements.sdk.model.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A registered payment-provider schema identifier.")
public record ProductSkuSchema(

        @Schema(description = "The database id of this Product SKU Schema.")
        String id,

        @NotNull
        @Schema(description = "The purchase provider schema in reverse-dns notation, e.g. com.apple.appstore.")
        String schema

) {

    /** Returns a copy of this record with the given database id set. */
    public ProductSkuSchema withId(final String id) {
        return new ProductSkuSchema(id, schema);
    }

}
