--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/1/18
-- Time: 3:02 PM
-- To change this template use File | Settings | File Templates.
--

local namazu_util = require "eci.util"
local namazu_index = require "eci.index"
local namazu_resource = require "eci.resource"
local namazu_response_code = require "eci.response.code"

local link_and_list = {}

function link_and_list.make_links(patha, pathb)

    local rid = namazu_resource.id()
    patha = namazu_util.path("test_case", patha, rid)
    pathb = namazu_util.path("test_case", pathb, rid)

    namazu_index.link(rid, patha)
    namazu_index.link(rid, pathb)

end

function link_and_list.say_hello()
    print("Hello World!")
end

return link_and_list
