
local coroutine = require "coroutine"

local echo = {}

function echo.echo(to_echo)
    coroutine.yield("COMMIT")
    return to_echo
end

return echo
