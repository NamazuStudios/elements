
local example_model = {}

example_model.foo = {

    description = "Example Simple (Foo) Object",

    properties = {

        -- Example string type named "string"
        string_property = {
            description = "An example string Property.",
            type = "string"
        },

        -- Example number type nmaed "foo_number"
        number_property = {
            description = "An example number Property.",
            type = "number"
        },

        -- Example boolean type named "foo_boolean"
        boolean_property = {
            description = "An example boolean Property.",
            type = "boolean"
        }

    }

}

-- A complex model which contains a reference to a foo_model object as
-- well as a reference to an array of foo_model objects.

example_model.bar = {

    description = "Example Complex (Bar) Object",

    properties = {

        object_property = {
            description = "An example object property",
            type = "object",
            model = "foo"
        },

        array_property = {
            description = "An example array property",
            type = "array",
            model = "foo"
        }

    }

}

return example_model;
