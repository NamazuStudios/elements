--
-- Created by IntelliJ IDEA.
-- User: garrettmcspadden
-- Date: 12/10/20
-- Time: 3:53 PM
-- To change this template use File | Settings | File Templates.
--
local manifest = {}

manifest.event = {
    ["test.event"] = {
        {module = "test.event", method = "hello_event"},
        {module = "test.event", method = "hello_event_again"}
    }
}

return manifest