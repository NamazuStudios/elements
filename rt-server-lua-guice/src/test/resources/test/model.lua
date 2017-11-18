--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/17/17
-- Time: 6:34 PM
-- To change this template use File | Settings | File Templates.
--

local model = require "namazu.model"

local test_model = {}

function test_model.test_array()
    return model.array{}
end

function test_model.test_object()
    return model.object{}
end

function test_model.test_array_default()
    return { "a", "b", "c" }
end

function test_model.test_object_default()
    return {}
end

return test_model
