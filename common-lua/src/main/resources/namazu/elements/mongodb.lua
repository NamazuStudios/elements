---
--- Created by keithhudnall.
--- DateTime: 3/22/22 11:34 AM
---

local ioc = require "namazu.ioc.resolver"
local connect_uri = ioc:inject("java.lang.String", "com.namazustudios.socialengine.mongo.uri")
local database_name = ioc:inject("java.lang.String", "com.namazustudios.socialengine.mongo.database.name")
local application_id = ioc:inject("com.namazustudios.socialengine.rt.id.ApplicationId"):asString()
local mongo = require "mongo"


local mongodb = {
    client = mongo.Client(connect_uri)
}


function mongodb.elements_database()

    return mongodb.client:getDatabase(database_name)
end


function mongodb.application_collection()

    local db = mongodb.elements_database()

    if(db:hasCollection(application_id)) then

        return db:getCollection()
    end

    return db:createCollection(application_id)
end


return mongodb