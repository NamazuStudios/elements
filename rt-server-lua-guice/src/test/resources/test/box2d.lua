--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/30/17
-- Time: 10:44 PM
-- To change this template use File | Settings | File Templates.
--
local Box2D = require "Box2D"

local test_box2d = {}

local function dump(prefix, table)
    for k, v in pairs(table) do
        if (type(v) == "table") then
--            dump(prefix .. "." .. tostring(k), v)
        else
            print(prefix .. "." .. tostring(k) .. " - " .. type(v) .. ": " .. tostring(v))
        end
    end
end

function test_box2d.test_hello_world()

    -- This is a Lua port of the Box2D Hello World availble from the project's github repo.  Can be found here:
    -- https://github.com/behdad/box2d/blob/master/Box2D/HelloWorld/HelloWorld.cpp

    -- Define the gravity vector.
    local gravity = Box2D.b2Vec2(0,-9.8);

    -- Construct a world object, which will hold and simulate the rigid bodies.
    local world = Box2D.b2World(gravity)

    local ground_body_def = Box2D.b2BodyDef()
    ground_body_def.position:Set(0, -10);

    -- Call the body factory which allocates memory for the ground body
    -- from a pool and creates the ground box shape (also from a pool).
    -- The body is also added to the world.

    local ground_body = world:CreateBody(ground_body_def)

    -- The extents are the half-widths of the box.
    local ground_box = Box2D.b2PolygonShape()
    ground_box:SetAsBox(50, 10);

    -- Add the ground fixture to the ground body.
    ground_body:CreateFixture(ground_box, 0)

    -- Define the dynamic body. We set its position and call the body factory.
    local body_def = Box2D.b2BodyDef();
    body_def.type = Box2D.b2_dynamicBody;
    body_def.position:Set(0, 4)

    local body = world:CreateBody(body_def)

    -- Define another box shape for our dynamic body.
    local dynamic_box = Box2D.b2PolygonShape()
    dynamic_box:SetAsBox(1, 1);

    -- Define the dynamic body fixture.
    local fixture_def = Box2D.b2FixtureDef()
    fixture_def.shape = dynamic_box

    -- Set the box density to be non-zero, so it will be dynamic.
    fixture_def.density = 1.0

    -- Override the default friction.
    fixture_def.friction = 0.3;

    -- Add the shape to the body.
    body:CreateFixture(fixture_def);

    -- Prepare for simulation. Typically we use a time step of 1/60 of a
    -- second (60Hz) and 10 iterations. This provides a high quality simulation
    -- in most game scenarios.
    local time_step = 1.0 / 60
    local velocity_iterations = 6
    local position_iterations = 2

    -- This is our little game loop.
    for i = 1,60 do

        -- Instruct the world to perform a single step of simulation.
        -- It is generally best to keep the time step and iterations fixed.
        world:Step(time_step, velocity_iterations, position_iterations);

        -- Now print the position and angle of the body.
        local position = body:GetPosition();
        local angle = body:GetAngle();

        -- Format the message
        local msg = string.format("%4.2f %4.2f %4.2f\n", position.x, position.y, angle);
        print(msg)

    end

end

return test_box2d
