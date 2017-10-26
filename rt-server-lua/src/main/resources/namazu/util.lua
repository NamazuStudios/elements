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
local Path = java.require("com.namazustudios.socialengine.rt.Path")

local table = require "table"

local util = {}

--- Returns a Path object.
-- This can accept a single string, which will be parsed out accordingly.  Alternatively,
-- this accepts variadic arguments which will specify the individual components of the Path.  See the constructor
-- for Path for more information.
-- @param ... the path components or a single string representing the path
function util.path(...)
    return Path:new(table.unpack(...))
end

--- Returns a sequence from a table
-- Accepting a single table, this will iterate the table, discard the keys, and sequene the table.  Some table, or
-- table like objects, will be sequenced properly based on their natural ordering.  Note standard lua tables do not work
-- with this method because they are
-- @param t the tablke to sequence
function util.sequence(t)

    local i = 1
    local seq = {}

    for k, v in pairs(t)
    do
        seq[i] = v
        i = i + 1
    end

    return seq

end

function util.uuid()
    return UUID:randomUUID():toString()
end

return util
