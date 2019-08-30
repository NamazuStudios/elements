
local coroutine = require "coroutine"

local echo = {}

function echo.commit(to_echo)
    echo.to_echo = to_echo
    coroutine.yield("COMMIT")
end

function echo.echo(test_param)
    return echo.to_echo
end

return echo
