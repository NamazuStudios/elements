--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/1/18
-- Time: 2:59 PM
-- To change this template use File | Settings | File Templates.
--

local namazu_util = require "eci.util"
local namazu_index = require "eci.index"
local namazu_resource = require "eci.resource"
local namazu_response_code = require "eci.response.code"
local namazu_model = require "eci.model"

local link_and_list = {}

function link_and_list.test_create(patha, pathb)

    local root = namazu_util.path("test_case", namazu_util.uuid())
    local rid, code = namazu_resource.create("test.link.resource", root)

    assert(code == namazu_response_code.OK, "Expected OK response code.  Got " .. tostring(code))
    namazu_resource.invoke(rid, "make_links", patha, pathb)
    namazu_index.unlink(root)

    return {root, rid}

end

function link_and_list.test_list(suffix)

    local path = namazu_util.path("test_case", suffix, "*")
    local result, code = namazu_index.list(path)
    assert(code == namazu_response_code.OK, "Expected OK response code.  Got " .. tostring(code))

    return result

end

return link_and_list
