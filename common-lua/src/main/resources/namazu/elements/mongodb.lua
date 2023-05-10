---
--- Created by keithhudnall.
--- DateTime: 3/22/22 11:34 AM
---

local ioc = require "namazu.ioc.resolver"
local connect_uri = ioc:inject("java.lang.String", "dev.getelements.elements.mongo.uri")
local elements_database_name = ioc:inject("java.lang.String", "dev.getelements.elements.mongo.database.name")

local mongo = require "mongo"


local mongodb = {
    client = mongo.Client(connect_uri)
}

local function get_database(db_name)

    local db, error = mongodb.client:getDatabase(db_name)

    return db, error
end


-- Gets the Elements default database.
function mongodb.get_elements_database()

    return get_database(elements_database_name)
end


-- Gets the database for the given name.
function mongodb.get_database(db_name)

    return get_database(db_name)
end


-- Gets the collection for the given name from the specified database.
-- Will create the collection if it doesn't exist.
function mongodb.get_collection(db, collection_name)

    local c, error = db:hasCollection(collection_name) and
            db:getCollection(collection_name) or
            db:createCollection(collection_name)

    return c, error
end


return mongodb