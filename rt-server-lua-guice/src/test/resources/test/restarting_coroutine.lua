--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 4/2/18
-- Time: 1:47 PM
-- To change this template use File | Settings | File Templates.
--

-- Integration tests to cover http://jira.namazustudios.net/browse/SOC-139 where a coroutine simply locks up the
-- resource until it's complete.  Essentially only one coroutine may run at a time which doesn't work.

local coroutine = require "coroutine"
local namazu_coroutine = require "eci.coroutine"

local task_id = nil
local finished = false

local restarting_coroutine = {}

local function do_start()

    finished = false;

    local co = coroutine.create(function()
        print "Started coroutine."
        local reason = coroutine.yield("FOR", 1, "SECONDS")
        finished = true;
        print ("Got resume reason " .. reason)
    end)

    task_id = namazu_coroutine.start(co)
    return task_id

end

function restarting_coroutine.start()
    do_start()
end

function restarting_coroutine.resume()

    print "Waiting for task id to report."

    while task_id == nil do
        coroutine.yield("FOR", 250, "MILLISECONDS")
    end

    namazu_coroutine.resume(task_id, "BAIL")

    print "Waiting for first to finish."

    while not finished do
        coroutine.yield("FOR", 250, "MILLISECONDS")
    end

    print "Restarting coroutine."

    task_id = do_start()

    print "Waiting for second to finish."

    while not finished do
        coroutine.yield("FOR", 500, "MILLISECONDS")
    end

    print "Finished."

end

return restarting_coroutine
