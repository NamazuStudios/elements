--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/20/17
-- Time: 1:19 AM
-- To change this template use File | Settings | File Templates.
--

local resource = {}

local ioc_resolver = require "namazu.ioc.resolver"
local resource_loader = ioc_resolver:inject("com.namazustudios.socialengine.rt.ResourceLoader")
local resource_service = ioc_resolver:inject("com.namazustudios.socialengine.rt.ResourceService")

function resource.create(path, module, ...)
    -- TODO Implement
end

function resource.invoke(resource_id, method_name, ...)
    -- TODO Implement
end

function resource.invoke_path(path, method_name, ...)
    -- TODO Implement
end

return resource
