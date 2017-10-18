--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/17/17
-- Time: 4:53 PM
-- To change this template use File | Settings | File Templates.
--

local pagination = {}

function pagination.of(model)
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
