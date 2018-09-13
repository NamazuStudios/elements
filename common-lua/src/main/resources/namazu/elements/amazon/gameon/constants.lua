--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/11/18
-- Time: 6:29 PM
-- To change this template use File | Settings | File Templates.
--

local function define_enums(target, enum_t)
    for e in ipairs(enum_t:values()) do
        local value = tostring(e)
        target[value] = value
    end
end

local constants = {}

--- The API Key Header Required by Amazon
constants.API_VERSION = "v1"
constants.API_KEY_HEADER = "x-api-key"
constants.USER_BASE_URI = "https://api.amazongameon.com/" .. constants.API_VERSION
constants.ADMIN_BASE_URI = "https://admin-api.amazongameon.com/" .. constants.API_VERSION

constants.app_build_type = {}
define_enums(constants.app_build_type, java.require "com.namazustudios.socialengine.model.gameon.game.AppBuildType")

constants.device_os_type = {}
define_enums(constants.device_os_type, java.require "com.namazustudios.socialengine.model.gameon.game.DeviceOSType")

return constants
