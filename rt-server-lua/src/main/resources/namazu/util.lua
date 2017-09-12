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

local Path = java.require("com.namazustudios.socialengine.rt.Path")

local namazu_util = {}

-- Returns a Path object.  This can accept a single string, which will be parsed out accordingly.  Alternatively,
-- this accepts variadic arguments which will specify the individual components of the Path.  See the constructor
-- for Path for more information.

function namazu_util.path(...)
    if (#arg == 1) then
        return Path:new(table[1])
    else
        return Path:new(arg)
    end
end

return namazu_util
