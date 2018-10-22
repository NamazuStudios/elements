--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 12/6/17
-- Time: 12:10 AM
-- To change this template use File | Settings | File Templates.
--

local user_dao = require "namazu.socialengine.dao.user"

local example_integration_test = {}

function example_integration_test.do_test()
    local root = user_dao.get_active_user_by_name_or_email("root")
    assert(root ~= nil, "Expected non-nil user for root user")
end

function example_integration_test.do_test_again()
    local root = user_dao.get_active_user_by_name_or_email("root")
    assert(root ~= nil, "Expected non-nil user for root user")
end

return example_integration_test
