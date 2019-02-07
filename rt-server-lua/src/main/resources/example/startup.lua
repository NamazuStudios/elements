
local os = require "os"
local coroutine = require "coroutine"
local namazu_response = require "namazu.response"
local http_status = require "namazu.http.status"
local pagination = require "namazu.pagination"


local startup = {}

function startup.run_once()
    print("ran once inside lua!")
end

function startup.run_forever()
    print("starting up run forever....")
    while(true)
    do
        reason, elapsed, units = coroutine.yield("UNTIL_NEXT", "* * * ? * *")
        print(reason, " yielded for ", elapsed, " ", units)
    end
end

return startup
