-- Default main file for the lua appplication.  The Lua application's methods and corresponding
-- scripts are provided here.  This is nothing more than a mapping of scripts which will be
-- included in the application.

-- Date: 8/8/17
-- Time: 11:21 PM

-- This table determines the HTTP mapping of methods to Lua modules.  Modules not specified here
-- will not be mapped to HTTP method calls.


-- A model definition object includes a definition of what an object looks like.  The types
-- correspond to the pre-defined lua types.  They key in the table is the name of the field
-- and the value is the type of value (eg, string, number).  In the cases of complex types,
-- the type a table indicating the type and referenced module object must be used.

-- A simple example model which contains primitives

manifest.model.foo = {

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

manifest.model.bar = {

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

-- The table containing the HTTP method manifest.

manifest.http = {

    -- The simple hello world operation

    -- Source code of the module, relative to the project root.  The key of the module map is the name of the module
    -- as it would be passed to require.  In this case it will load example/hello_world.lua for this particular module
    -- when in the process of servicing the request

    ["example.hello_world"] = {


        -- The operations contained in the module.  This maps the request/respons metadata to various
        -- Lua script methods based on the nature of the request/response.

        operations = {

            get_hello_world = {

                -- Describes the operation which will be relayed to documentation definitions
                description = "Example Operation",

                -- The corresponding HTTP verb.  Must be one of GET, POST, PUT, DELETE, HEAD, OPTIONS.
                -- Note:  If left unspecified, HEAD or OPTIONS will revert to a default behavior.  Thereby
                -- obviating the need to implement those under normal circumstances.
                --
                -- In the case of HEAD, a GET request will execute (if available) and then provide the
                -- response without the headers.
                --
                -- The OPTIONS request will use the operations specified in this manifest file to determine
                -- what requests are availble wihtout having to provide a specific implementation.

                verb = "GET",

                -- The path the server will resolve the module.  The path parameters encapsulated in the
                -- {} notation and will be used as wildcard-style matching.  This example will match
                -- any path under /hello_world/ and capture the remaining path componenet in the
                -- foo path parameter.

                path = "/hello_world/{foo}",

                -- The lua method to call in the module when servicing the request.  This will include
                -- the get module.

                method = "get",

                -- Parameters which the request will accept.  Parameters may ony specify simple types.

                parameters = {
                    foo_number = "number",
                    bar_number = "number",
                    foo_string = "string",
                    bar_string = "string"
                },

                -- Specifies the content which will be produced and consumed by the operation.  The consumer
                -- will consume a model of the supplied type when the content type is provided.

                consumes = {

                    -- Specifies the content-type to match the incoming requests
                    ["application/json"] = {

                        -- Specifies the model which will be used to service the request
                        model = "foo",

                        -- Specifies any additional headers this request may consume
                        headers = {

                            ["X-MyExampleStringHeader"] = {
                                description = "An example string header",
                                type = "string",
                            },

                            ["X-MyExampleNumberHeader"] = {
                                description = "An example number header",
                                type = "string",
                            },

                            ["X-MyExampleBooleanHeader"] = {
                                description = "An example boolean header",
                                type = "string",
                            }

                        }

                    }
                },

                produces = {

                    -- Specifies the content-type to match to the
                    ["application/json"] = {

                        -- Specifies the model which will be used to service the request
                        model = "foo",

                        -- Specifies any additional headers this request may consume
                        headers = {
                            ["X-MyExampleStringHeader"] = {
                                description = "An example string header",
                                type = "string",
                            },

                            ["X-MyExampleNumberHeader"] = {
                                description = "An example number header",
                                type = "string",
                            },

                            ["X-MyExampleBooleanHeader"] = {
                                description = "An example boolean header",
                                type = "string",
                            }
                        },

                        -- Specifies any options headers, if availble

                        static_headers = {
                            ["Access-Control-Allow-Origin"] = "http://example.com"
                        }

                    }

                },

            }
        }

    }

}
