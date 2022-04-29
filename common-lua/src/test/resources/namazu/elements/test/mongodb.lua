---
--- Created by keithhudnall.
--- DateTime: 3/22/22 3:32 PM
---

local mongodb = require "namazu.elements.mongodb"

local mongodb_test = {}


function mongodb_test.test_get_elements_database()

    local success, error = mongodb.elements_database()

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_get_application_collection()

    local success, error = mongodb.application_collection()

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_get_users_collection()

    local db = mongodb.elements_database()
    local success, error = db:getCollection("user")

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_create_application_entries()

    local c = mongodb.application_collection()

    local entry_one = {
        ["_id"] = "1",
        ["name"] = "entry_one"
    }

    local entry_two = {
        ["_id"] = "2",
        ["name"] = "entry_two"
    }

    --Returns true. On error, returns nil and the error message.
    local success, error = c:insertMany(entry_one, entry_two)

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_modify_application_entries()

    local c = mongodb.application_collection()
    local query = '{"_id":"1"}'
    local new_name = "new_name"
    local success, error = c:update(query, {name = new_name})

    if(success) then

        success, error = c:findOne(query)

        if(success) then

            local entry = success:value()

            if(entry.name == new_name) then
                return nil
            end

            return "Update not successful!"
        end
    end

    return error
end


function mongodb_test.test_delete_application_entries()

    local c = mongodb.application_collection()

    local success, error = c:removeMany('')

    --Return error if fail or nil if success
    return success == nil and error or nil
end

return mongodb_test