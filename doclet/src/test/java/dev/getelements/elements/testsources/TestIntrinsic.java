package dev.getelements.elements.testsources;

import dev.getelements.elements.rt.annotation.*;

@Intrinsic(
    value = @ModuleDefinition("namazu.intrinsic.test"),
    authors = "ptwohig",
    summary = "Intrinsic Test Code",
    description = "This API exists to test intrinsic documentation.",
    methods = {
        @MethodDefinition(
            value = "foo",
            summary = "Does a Foo.",
            description = "A method that does the foo!",
            parameters = {
                @ParameterDefinition(value="a", type = "string", comment = "The String to Foo"),
                @ParameterDefinition(value="b", type = "number", comment = "The Number to Foo")
            },
            returns = {
                @ReturnDefinition(comment = "The first Foo Result", type = "string"),
                @ReturnDefinition(comment = "The second Foo Result", type = "number")
            }
        )
    }
)
public class TestIntrinsic {}
