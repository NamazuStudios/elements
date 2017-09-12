package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResponseCode;

/**
 * A set of constants used by the {@link LuaResource} and related classes.
 *
 * Created by patricktwohig on 11/2/15.
 */
public interface Constants {

    /**
     * The print function name.  This hijacks the "regular" print function and diverts its output
     * to the script's log.  The script's Log is actually backed by slf4j
     */
    String PRINT_FUNCTION = "print";

    /**
     * The name of a global table where the Lua code interacts with the
     * underlying server APIs.
     */
    String NAMAZU_RT_TABLE = "namazu_rt";

    /**
     * The name of the namazu_rt close() function which will be called just before the resource
     * is closed.  This is useful in case the underlying lua script needs to release or free
     * any resources before it is destroyed.
     */
    String CLOSE_FUNCTION = "close";

    /**
     * A table housing the response codes as defined by {@link ResponseCode#getCode()}
     */
    String RESPONSE_CODE = "response_code";

    /**
     * This is the table name under namazu_rt that defines the init parameters for the script.
     */
    String INIT_PARAMS = "init_params";

    /**
     * Constant to designate the server.coroutine table.  This is a set of coroutinee
     * functions managed by the server that are housed internally in the script.  The
     * client code should not interfere with the running co-routines
     */
    String COROUTINE_TABLE = "coroutine";

    /**
     * The name of the server.coroutine.create() function.
     */
    String COROUTINE_CREATE_FUNCTION = "create";

    /**
     * A registry table for server threads.  This is stored in the lua registry and is not
     * visible to the Lua source code at all.
     */
    String SERVER_THREADS_TABLE = "NAMAZU_RT_SERVER_THREADS";

    /**
     * A key on the services table to expose  an instance of the Resource type to the underlying Lua script.  This
     * allows the underlying script to perform functions such as getting the current path, or posting
     * events to subscribers.
     */
    String THIS_INSTANCE = "resource";

    /**
     * The "package" table.  See the Lua manual for what this is used for.
     */
    String PACKAGE_TABLE = "package";

    /**
     * The "package.searchers" table.  See the Lua manual for what this is used for.
     */
    String PACKAGE_SEARCHERS_TABLE = "searchers";

    /**
     * Exposes the instance of {@link IocResolver} which the underlying script can use to resolve dependencies
     * such as other instances of {@link Resource}.
     */
    String IOC_INSTANCE = "ioc";

}
