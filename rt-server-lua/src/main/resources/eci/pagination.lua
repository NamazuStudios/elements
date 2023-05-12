--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/17/17
-- Time: 6:25 PM
-- To change this template use File | Settings | File Templates.
--

local model = require "eci.model"

local pagination = {}

function pagination.of(offset, total, objects)
    return {
        ["offset"] = offset,
        ["total"] = total,
        ["objects"] = model.array(objects)
    }
end

function pagination.manifest_for(model)
    return {

        description = "Pagination of " .. model,

        properties = {

            offset = {
                description = "The starting offset in the dataset of " .. model .. " objects",
                type = "number"
            },

            total = {
                description = "The total number of " .. model .. " objects in the dataset",
                type = "number"
            },

            objects = {
                description = "The list of " .. model .. " objects",
                type = "array",
                model = model
            }

        }

    }
end

return pagination
