
-- A very simple script which represents a clock.  This is to demonstrate
-- an internal resource's ability to produce events and respond to requests
-- made from other resources.  This effectively, instanties some metadata from
-- the underlying JVM and then produces events that tell the clocks's time
-- in it's current zone.

-- This also demonstrates the usage of the corountines to make timers to produce
-- the events.

require "namazu_event"
require "namzu_time"

TimeZone = java.require "java.util.TimeZone"
SimpleDateFormat = java.require "java.text.SimpleDateFormat"
Date = java.require "java.util.Date"

-- Remember the init_params were passed into the creation routine by the resource
-- edge resource that created this resource
clockTimeZone = TimeZone:getTimeZone(namazu_rt.init_params.time_zone)

-- Start coroutines to produce the events.  For this we issue two.  We issue a "tick tock"
-- every second and a "ding dong" every hour.  What a clock tower would do.

namazu_rt.coroutine.create(function (deltaTime)

    -- This will simply "tick tock" every one second.  We start a loop, yield for the
    -- amount of time and then post the event

    simpleDateFormat = SimpleDateFormat:new("yyyy.MM.dd G 'at' HH:mm:ss z")
    simpleDateFormat:setTimeZone(clockTimeZone)

    while (true) do

        coroutine.yield(1)

        namazu_event.post("tick tock", {
            time = simpleDateFormat:format(Date:new())
        })

    end

end)

namazu_rt.coroutine.create(function (deltaTime)

    -- This will simply "ding dong" every half hour.  The catch is that the server

    simpleDateFormat = SimpleDateFormat:new("yyyy.MM.dd G 'at' HH:mm:ss z")
    simpleDateFormat:setTimeZone(clockTimeZone)

    while (true) do

        -- The given cron expression will calculate the number of seconds until
        -- the expression should trigger.  Therefore, we pass this into the yield
        -- function such that the server will know to wake this coroutine up only
        -- according to the supplied schedule.

        coroutine.yield(namazu_time.until_next("0 * * * *"))

        namazu_event.post("ding dong", {
            time = simpleDateFormat:format(Date:new())
        })

    end

end)
