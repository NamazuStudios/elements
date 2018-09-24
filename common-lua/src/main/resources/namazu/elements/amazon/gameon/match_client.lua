--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/14/18
-- Time: 3:51 PM
-- To change this template use File | Settings | File Templates.
--

local log = require "namazu.log"
local http_client = require "namazu.http.client"

local math = require "math"
local gameon_constants = require "namazu.elements.amazon.gameon.constants"

local match_client = {}

match_client.PATH = "/matches"
match_client.SCORE_PATH = "/score"

function match_client:new(o)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

--- Submits a Score to the Match
-- This submits the score to the match.
--
-- @param score the score (must be an integer)
function match_client:submit_score(score)

    print("Score " .. tostring(score))

    local request = {
        method = "PUT",
        base = gameon_constants.USER_BASE_URI,
        path = match_client.PATH .. "/" .. self.matchId .. match_client.SCORE_PATH,
        headers = {
            [gameon_constants.SESSION_ID_HEADER] = self.sessionId,
            [gameon_constants.API_KEY_HEADER] = self.sessionApiKey
        },
        entity = {
            media_type = "application/json",
            value = {
                score = math.floor(score)
            }
        }
    }

    local status, headers, response = http_client.send(request)

    if (status ~= 200)
    then
        local message = response and response.message or "<unknown>"
        log.error("HTTP Status {}. Response {}.  Message {}", status, response, message)
        error{ status = status, message = message }
    end

end

return match_client
