--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/17/17
-- Time: 6:12 PM
-- To change this template use File | Settings | File Templates.
--

local model = {}

--- Tags the supplied table an "array" type
-- This will set a metatable hint indicating that the supplied value should be treated as an array type for
-- serialization purposes.  Though not strictly required, the supplied table should be a sequence.  When serialized
-- the array (or array like) tables will have the keys discarded and only the values serialized.
--
-- @param value the table which to tag as an array
function model.array(value)

    local mt = getmetatable(value)

    if (mt == nil)
    then
        mt = {}
    end

    mt.__namazu_manifest_type = "array"
    setmetatable(value, mt)

    return value

end

--- Tags the supplied table an "object" type
-- This will set a metatable hint indicating that the supplied value should be treated as an array type for
-- serialization purposes.  Though not strictly required, the supplied table should be a sequence.  When serialized
-- the array (or array like) tables will have the keys discarded and only the values serialized.
--
-- @param value the table which to tag as an array
function model.object(value)

    local mt = getmetatable(value)

    if (mt == nil)
    then
        mt = {}
    end

    mt.__namazu_manifest_type = "object"
    setmetatable(value, mt)

    return value

end

return model
