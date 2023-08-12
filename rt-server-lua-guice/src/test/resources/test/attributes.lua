
local attributes = require "eci.resource.attributes"

local module = {}

function module.check_default_attributes()

    local foo = attributes:getAttribute("dev.getelements.foo")
    local bar = attributes:getAttribute("dev.getelements.bar")
    local extra = attributes:getAttribute("dev.getelements.extra")
    local override = attributes:getAttribute("dev.getelements.override")

    assert(foo == "foo", foo .. " != \"foo\"")
    assert(bar == "bar", bar .. " != \"bar\"")

    assert(extra == nil)
    assert(override == "no", override .. " == \"no\"")

end

function module.check_override_attributes()

    local foo = attributes:getAttribute("dev.getelements.foo")
    local bar = attributes:getAttribute("dev.getelements.bar")
    local extra = attributes:getAttribute("dev.getelements.extra")
    local override = attributes:getAttribute("dev.getelements.override")

    assert(foo == "foo", foo .. " != \"foo\"")
    assert(bar == "bar", bar .. " != \"bar\"")

    assert(extra == "yes", extra .. " != \"yes\"")
    assert(override == "yes", override .. " == \"yes\"")

end

return module