-- Default main file for the lua appplication.  The Lua application's methods and corresponding
-- scripts are provided here.  This is nothing more than a mapping of scripts which will be
-- included in the application.

-- Date: 8/8/17
-- Time: 11:21 PM

-- This table determines the HTTP mapping of methods to Lua modules.  Modules not specified here
-- will not be mapped to HTTP method calls.
namazu.http = {

    -- Operations may be specified.  The key is the name of the API.

    hello_world = {

        -- The path the server will resolve the module.  Wildcards may be used to match several
        -- paths.  The methods will recieve the full request object.
        path = "/hello_world/*",

        -- Specifies the content type which will be both consumed and produced by the particular
        -- module.  The first entry in in the list is used as the default when the request is
        -- devoid of either hte Content-Type or the Accept header.

        consumes = { "application/json" },
        produces = { "application/json" },

        -- Source code of the module, relative to the project root.  This is optional and by
        -- default will map to the name of the operation set.  You may specify other modules
        -- consistent with Lua's "require" keyword such as "foo.bar" to indicate that the
        -- module's code resides in foo/bar.lua
        module = "hello_world",

        -- The methods to call in the module when servicing a request.  This is optional and if
        -- unspecified, then then the container will attempt to call a method in the module
        -- with the name matching the HTTP verb.  Unmapped method will simply map to a 404

        methods = {
            GET    = "get",
            PUT    = "put",
            POST   = "post",
            DELETE = "delete"
        }

    }

}
