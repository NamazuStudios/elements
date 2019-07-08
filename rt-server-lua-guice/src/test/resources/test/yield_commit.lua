--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 6/21/18
-- Time: 4:17 PM
-- To change this template use File | Settings | File Templates.
--

local coroutine = require "coroutine"

local yield_commit = {}

function yield_commit.test_commit()
    coroutine.yield("COMMIT")
end

function yield_commit.test_complex_commit()
    coroutine.yield("COMMIT")
    coroutine.yield("IMMEDIATE")
    coroutine.yield("COMMIT")
    coroutine.yield("IMMEDIATE")
    coroutine.yield("COMMIT")
    coroutine.yield("IMMEDIATE")
end

function yield_commit.test_repeat_commit()
    coroutine.yield("COMMIT")
    coroutine.yield("COMMIT")
    coroutine.yield("COMMIT")
end

return yield_commit
