--
-- A set of event functions used to manage events.  This is useful.
--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/30/15
-- Time: 3:55 PM
-- To change this template use File | Settings | File Templates.
--

local namazu_event = {}

-- Posts an event with the given name.  This uses the underlying Resource to send the event
-- and in all cases.
function namazu_event.post(name, payload)
    namazu_rt.resource:post(name, payload)
end

return namazu_event
