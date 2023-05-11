---
--- Created by keithhudnall.
--- DateTime: 3/22/22 3:32 PM
---

local mongodb = require "eci.elements.mongodb"

local mongodb_test = {}

local TEST_DB_NAME = "testDB"
local TEST_COLLECTION_NAME = "testCollection"

function mongodb_test.test_get_elements_database()

    local success, error = mongodb.get_elements_database()

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_get_application_collection()

    local db, error = mongodb.get_database(TEST_DB_NAME)

    if(error ~= nil) then
        return error
    end

    local success = mongodb.get_collection(db, TEST_COLLECTION_NAME)

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_get_users_collection()

    local db, error = mongodb.get_elements_database()

    if(error ~= nil) then
        return error
    end

    local success, error = mongodb.get_collection(db, "user")

    --Return error if fail or nil if success
    return success == nil and error or nil
end


function mongodb_test.test_create_application_entries()

    local db, error = mongodb.get_database(TEST_DB_NAME)

    if(error ~= nil) then
        return error
    end

    local c = mongodb.get_collection(db, TEST_COLLECTION_NAME)

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

    local db, error = mongodb.get_database(TEST_DB_NAME)
    local c = mongodb.get_collection(db, TEST_COLLECTION_NAME)

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

    local db, error = mongodb.get_database(TEST_DB_NAME)

    if(error ~= nil) then
        return error
    end

    local c, error = mongodb.get_collection(db, TEST_COLLECTION_NAME)

    if(error ~= nil) then
        return error
    end

    local pre_count = c:count({})

    if(pre_count == 0) then
        return string.format("Bad test: Collection is empty!")
    end

    local success, error = c:removeMany({})
    local post_count = c:count({})

    if(post_count ~= 0) then
        return string.format("Count after deletion (%d) is greater than 0!", post_count)
    end

    --Return error if fail or nil if success
    return error == nil and error or nil
end

return mongodb_test