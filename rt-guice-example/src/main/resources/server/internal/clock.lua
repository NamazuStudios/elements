
-- A very simple script which represents a clock.  This is to demonstrate
-- an internal resource's ability to produce events and respond to requests
-- made from other resources.  This effectively, instanties some metadata from
-- the underlying JVM and then produces events that tell the clocks's time
-- in it's current zone.

-- This also demonstrates the usage of the corountines to make timers to produce
-- the events.

require "namazu_event"

TimeZone = java.require("java.util.TimeZone")


-- Remember the init_params were passed into the creation routine by the resource
-- edge resource that created this resource
clockTimeZone = TimeZone:getTimeZone(namazu_rt.init_params.time_zone)

-- Start coroutines to produce the events.  For this we issue two.  We issue a "tick tock"
-- every second and a "ding dong" every half hour.  What a clock tower would do.