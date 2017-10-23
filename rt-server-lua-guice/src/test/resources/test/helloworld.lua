--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 3:49 PM
-- To change this template use File | Settings | File Templates.
--

local helloworld = {}

function helloworld.knock_knock()
    return "Who's there?"
end

function helloworld.identify(who)
    return who == "Interrupting Cow - Moo!"
end

return helloworld
