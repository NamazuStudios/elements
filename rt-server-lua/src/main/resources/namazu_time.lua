--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/3/15
-- Time: 1:45 AM
-- To change this template use File | Settings | File Templates.
--
-- A set of functions useful for manipulating and processing time.

local namazu_time

local server = namazu_rt.ioc:inject("com.namazustudios.socialengine.rt.Server")

local CronParser = java.require "com.cronutils.parser.CronParser"
local ExecutionTime = java.require "com.cronutils.model.time.ExecutionTime"
local DateTime = java.require "org.joda.time.DateTime"

-- Returns the current server time in seconds
function namazu_time.server_time()
    return server:getServerTime();
end

-- Returns the time since the UNIX epoch in seconds.
function namazu_time.time_since_epoch()
    return server:getTimeSinceEpoch();
end

-- Returns the amount of time until the next triggering of the given cron
-- expression.  This value is expressed in seconds.
function namazu_time.until_next(cronExpression)
    parser = CronParser:new()
    cron = parser:parse(cronExpression)
    executionTime = ExecutionTime:forCron(cron)
    duration = executionTime:timeToNextExecution(DateTime:now())
    return getMillis:getMillis() / 1000;
end

return namazu_time;
