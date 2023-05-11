--
-- Created by IntelliJ IDEA.
-- User: garrettmcspadden
-- Date: 12/10/20
-- Time: 3:45 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "eci.util"
local test_java_event = require "test.java.event"

local event = {}

function event.hello_event()
    print("hello test event from inside lua!")
    test_java_event.hello_world_event()
end

function event.hello_event_again()
    print("hello test event again from inside lua!")
    test_java_event.hello_world_event()
end

function event.who(who)
    print("who event from inside lua!")
    print("Identifying \"who\" " .. who)
    test_java_event.who(who)
end

function event.who_with_count(who, count)
    print("who event with count!")
    for i = 1, count
    do
        print("Identifying \"who\" " .. who[i])
        test_java_event.who_with_count(who[i], count)
    end
end

return event

