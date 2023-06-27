--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 4/2/18
-- Time: 1:47 PM
-- To change this template use File | Settings | File Templates.
--

-- Integration tests to cover http://jira.namazustudios.net/browse/SOC-139 where a coroutine simply locks up the
-- resource until it's complete.  Essentially only one coroutine may run at a time which doesn't work.

local test_coroutine = {}

local coroutine = require "coroutine"
local namazu_coroutine = require "eci.coroutine"

local task_id = nil

function test_coroutine.block()

    task_id = namazu_coroutine.current_task_id()

    print ("Blocking coroutine " .. tostring(task_id) .. " indefinitely.")
    coroutine.yield("INDEFINITELY")

    print ("Got wake signal.  Exiting.")
    return "OK"

end

function test_coroutine.awake()

    while task_id == nil do
        print("No corresponding task.  Waiting ...")
        coroutine.yield("FOR", 1, "SECONDS")
    end

    print ("Waking up " .. tostring(task_id))
    namazu_coroutine.resume(task_id)

    return "OK"

end

function test_coroutine.short_yield()
    local id = namazu_coroutine.current_task_id()
    print ("Blocking coroutine " .. tostring(id) .. " for 1 second.")
    coroutine.yield("FOR", 1, "SECONDS")
    return "OK"
end

return test_coroutine
