--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/17/17
-- Time: 6:25 PM
-- To change this template use File | Settings | File Templates.
--

local table = require "table"

local pagination = {}

function pagination.of_table(offset, total, objects)

    objects_sequence = table.unpack(3)

    return {
        offset = offset,
        total = total,
        objects = objects
    }

end

function pagination.of_sequence(offset, total, ...)
    objects_table = table.pack(arg, ...)
    return pagination.of_table(offset, total, objects_table)
end

return pagination
