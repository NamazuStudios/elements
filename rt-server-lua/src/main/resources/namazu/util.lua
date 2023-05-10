--
-- A set of utlitity fucnctions wrapping the Java API.  This mostly deals with common tasks
-- that other modules will require.
--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/30/15
-- Time: 9:16 AM
-- To change this template use File | Settings | File Templates.
--

local UUID = java.require("java.util.UUID")
local Path = java.require("dev.getelements.elements.rt.Path")

local table = require "table"

local util = {}

util.java = require "namazu.util.java"

--- Returns a Path object.
-- This can accept a single string, which will be parsed out accordingly.  Alternatively,
-- this accepts variadic arguments which will specify the individual components of the Path.  See the constructor
-- for Path for more information.
-- @param ... the path components or a single string representing the path
function util.path(...)
    return Path:new(table.pack(...)):toNormalizedPathString()
end

function util.uuid()
    return UUID:randomUUID():toString()
end

return util
