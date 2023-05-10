--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/25/17
-- Time: 10:16 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local resource = require "namazu.resource"

local auth = require "namazu.socialengine.auth"

local User = java.require "dev.getelements.elements.model.user.User"
local Profile = java.require "dev.getelements.elements.model.profile.Profile"

local responsecode = require "namazu.response.code"

local inventory_item_dao = require "namazu.elements.dao.inventoryitem"

local test_auth = {}

function test_auth.test_facebook_security_manifest()

    local manifest = auth.default_security_manifest();
    assert(manifest ~= nil, "Expected non-nil security manifest.")

    local header = manifest["header"]
    assert(header ~= nil, "Expected non-nil security manifest.")

    local session_secret_scheme = header[auth.SESSION_SECRET_SCHEME]
    assert(session_secret_scheme ~= nil, "Expected non-nil Facebook oauth scheme")

    local description, spec

    description, spec = session_secret_scheme["description"], session_secret_scheme["spec"]
    assert(spec ~= nil, "Session Secret spec nil.")
    assert(description ~= nil, "Session Secret description nil.")

    assert(spec["name"] == "Elements-SessionSecret", "Expected 'Elements-SessionSecret.'  Got: " .. tostring(spec["name"]))
    assert(spec["description"] == "The header containing the session secret.", "Got: " .. tostring(spec["description"]))
    assert(spec["type"] == "string", "Expected 'string'.  Got: " .. tostring(spec["type"]))

end

function test_auth.test_profile()

    local profile = auth.profile()

    assert(profile ~= nil, "Expected non-nil profile.")
    assert(profile.id == "ExampleProfileId", "Expected 'ExampleProfileId'.  Got " .. tostring(profile.id))
    assert(profile.displayName == "Example McExampleson", "Expected 'Example McExampleson'.  Got: " .. tostring(profile.displayName))
    assert(profile.imageUrl == "http://example.com/profile.png", "Expected 'http://example.com/profile.png'.  Got: " .. tostring(profile.imageUrl))

    assert(profile.user ~= nil, "Expected non-nil user.")
    assert(profile.user.id == "ExampleUserId", "Expected 'ExampleUserId'.  Got " .. tostring(profile.user.id))
    assert(profile.user.name == "example", "Expected 'example'.  Got " .. tostring(profile.user.name))
    assert(profile.user.email == "example@example.com", "Expected 'example@example.com'.  Got " .. tostring(profile.user.email))
    assert(profile.user.active, "Expected active user.")
    assert(tostring(profile.user.level) == "USER", "Expected 'USER'.  Got " .. tostring(profile.user.level))

    assert(profile.application ~= nil, "Expected non-nil application.")
    assert(profile.application.id == "ExampleApplicationId", "Expected 'ExampleApplicationId'.  Got " .. tostring(profile.application.id))
    assert(profile.application.name == "Example", "Expected 'Example'.  Got " .. tostring(profile.application.name))
    assert(profile.application.description == "Example Application", "Expected 'Example Application'.  Got " .. tostring(profile.application.description))

end

function test_auth.test_profile_unknown()
    local profile = auth.profile()
    assert(profile == nil, "Expected nil profile.  Got non-nil value." .. tostring(profile))
end

function test_auth.test_authenticated_user()

    local user = auth.user()

    assert(user ~= nil, "Expected non-nil user.")
    assert(user.id == "ExampleUserId", "Expected 'ExampleUserId'.  Got " .. tostring(user.id))
    assert(user.name == "example", "Expected 'example'.  Got " .. tostring(user.name))
    assert(user.email == "example@example.com", "Expected 'example@example.com'.  Got " .. tostring(user.id))
    assert(user.active, "Expected active user.")
    assert(tostring(user.level) == "USER", "Expected 'USER'.  Got " .. tostring(user.level))

end

function test_auth.test_unauthenticated_user()

    local user = auth.user()

    assert(user ~= nil, "Expected non-nil user.")
    assert(user.id == nil, "Expected nil.  Got " .. tostring(user.id))
    assert(user.name == "", "Expected ''.  Got " .. tostring(user.name))
    assert(user.email == "", "Expected ''.  Got " .. tostring(user.id))
    assert(not user.active, "Expected inactive user.")
    assert(tostring(user.level) == "UNPRIVILEGED", "Expected 'UNPRIVILEGED'.  Got " .. tostring(user.level))

end


local function make_resource()

    local attributes = {
        [User.USER_ATTRIBUTE] = auth.user(),
        [Profile.PROFILE_ATTRIBUTE] = auth.profile()
    }

    local path = "/test/auth/" .. util.uuid()
    local rid, code = resource.create("namazu.elements.test.auth", path, attributes)
    print("Created resource " .. tostring(rid) .. " (" .. code .. ") at path " .. path)
    return rid, code

end

function test_auth.test_authenticated_user_remote()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "test_authenticated_user")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))

end

function test_auth.test_profile_remote()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "test_profile")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))

end



function test_auth.test_auth_inventory()

    local user = auth.user();

    local result, credited = pcall(
        function ()
            return inventory_item_dao.adjust_quantity_for_item(user, "foo", 0, 100)
        end
    )

    assert(result, "Expected successful result.")

end

function test_auth.test_auth_inventory_remote()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "test_auth_inventory")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))

end


return test_auth
