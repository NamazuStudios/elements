--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/9/18
-- Time: 3:32 PM
-- To change this template use File | Settings | File Templates.
--

local detail = require "namazu.log.detail"

local log = {}

--- Logs a "trace" level message
-- Logs a message at the "trace" level.  Typically reserved for very fine-grained logging and debugging very detailed
-- issues.
--
-- The format string will be passed directly through.  Substituting {} for format arguments.
--
-- @param fmt the format string
-- @param ... remaining arguments
function log.trace(fmt, ...) end

--- Logs a "debug" level message
-- Logs a message at the debug level.  This is appropriate for most debug operations.  It is not enabled by default
-- under most circumstances but can be turned on when needed.
--
-- The format string will be passed directly through.  Substituting {} for format arguments.
--
-- @param fmt the format string
-- @param ... remaining arguments
function log.debug(fmt, ...) end

--- Logs an "info" level message
-- Logs an informational message.  This is typically turned on for most configurations.  This should be used spairingly
-- to avoid cluttering server logs.  This should be used to indicate messages pertaining to normal and healthy operation
-- of the server.
--
-- The format string will be passed directly through.  Substituting {} for format arguments.
--
-- @param fmt the format string
-- @param ... remaining arguments
function log.info(fmt, ...) end

--- Logs a "warn" level message
-- Logs an informational message.  This is typically turned on for most configurations.  This should be used spairingly
-- to avoid cluttering server logs.  This is used to indicate an internal, but possibly recoverable, failure in the
-- server.  This may be tied to external alets (such as email to administrators).
--
-- The format string will be passed directly through.  Substituting {} for format arguments.
--
-- @param fmt the format string
-- @param ... remaining arguments
function log.warn(fmt, ...) end

--- Logs an "error" level message
-- Logs an informational message.  This is typically turned on for most configurations.  This should be used spairingly
-- to avoid cluttering server logs.  This is used to indicate an internal, but likely unrecoverable, error failure in
-- the server.  This may be tied to external alerts (such as email to administrators).
--
-- The format string will be passed directly through.  Substituting {} for format arguments.
--
-- @param fmt the format string
-- @param ... remaining arguments
function log.error(fmt, ...) end

if detail.logger():isTraceEnabled()
then
    function log.trace(fmt, ...)
        detail.logger():trace(fmt, ...)
    end
end

if detail.logger():isDebugEnabled()
then
    function log.debug(fmt, ...)
        detail.logger():debug(fmt, ...)
    end
end

if detail.logger():isInfoEnabled()
then
    function log.info(fmt, ...)
        detail.logger():info(fmt, ...)
    end
end

if detail.logger():isWarnEnabled()
then
    function log.warn(fmt, ...)
        detail.logger():warn(fmt, ...)
    end
end

if detail.logger():isErrorEnabled()
then
    function log.error(fmt, ...)
        detail.logger():error(fmt, ...)
    end
end

return log
