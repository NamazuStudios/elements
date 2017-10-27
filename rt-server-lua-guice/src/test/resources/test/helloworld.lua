--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 3:49 PM
-- To change this template use File | Settings | File Templates.
--

local helloworld = {}

function helloworld.knock_knock()
    print("Got Knock Knock.  Returning \"Who's There?\"")
    return "Who's there?"
end

function helloworld.identify(who)

    print("Identifying \"who\"" .. who)

    if who == "Interrupting Cow - Moo!"
    then
        print("Correct punchline!")
        return true
    else
        print("Wrong punchline!")
        return false
    end

end

function helloworld.full_joke()
    return {
        setup = "Knock Knock",
        question = "Who's There?",
        punchline = "Interrupting Cow - Moo!"
    }
end

return helloworld
