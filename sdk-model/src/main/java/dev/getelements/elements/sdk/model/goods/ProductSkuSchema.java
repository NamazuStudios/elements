package dev.getelements.elements.sdk.model.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a registered payment-provider schema identifier.
 *
 * @param id the database id of this Product SKU Schema
 * @param schema the purchase provider schema in reverse-dns notation, e.g. com.apple.appstore
 */
@Schema(description = "A registered payment-provider schema identifier.")
public record ProductSkuSchema(

        @Schema(description = "The database id of this Product SKU Schema.")
        String id,

        @NotNull
        @Schema(description = "The purchase provider schema in reverse-dns notation, e.g. com.apple.appstore.")
        String schema

) {

    /**
     * Returns a copy of this record with the given database id set.
     *
     * @param id the database id to set
     * @return a new instance with the given id
     */
    public ProductSkuSchema withId(final String id) {
        return new ProductSkuSchema(id, schema);
    }

}
