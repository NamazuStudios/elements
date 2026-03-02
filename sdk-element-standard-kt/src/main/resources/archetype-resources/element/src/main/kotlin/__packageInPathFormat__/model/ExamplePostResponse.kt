package ${package}.model

import dev.getelements.elements.sdk.model.Constants
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Schema
class ExamplePostResponse {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "A unique name for the object that we're creating. No spaces allowed.")
    var name: String? = null

    @Schema(description = "The type of request being made. For example/debugging purposes.")
    var requestType: String = "ExamplePostResponse"

    @Schema(description = "Any additional information to return.")
    var metadata: Map<String, Any>? = null

}