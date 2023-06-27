--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/18/17
-- Time: 10:45 AM
-- To change this template use File | Settings | File Templates.
--

local pagination = require "eci.pagination"

local test_pagination = {}

function test_pagination.test_of()

    p = pagination.of(0, 3, {"A", "B", "C"})

    assert(tonumber(p.offset), "offset is not a number.")
    assert(tonumber(p.total), "total is not a number.")

    assert(type(p.objects) == "table", "objects is not a table")
    assert(p.offset == 0)
    assert(p.total == 3)

    assert(#p.objects == 3, "Expected table length of 3.  Got: " .. #p.objects)
    assert(p.objects[1] == "A", "1st element mismatch.  Expected A")
    assert(p.objects[2] == "B", "2nd element mismatch.  Expected B")
    assert(p.objects[3] == "C", "3rd element mismatch.  Expected C")

end

return test_pagination