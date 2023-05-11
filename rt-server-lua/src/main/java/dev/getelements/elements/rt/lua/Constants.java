package dev.getelements.elements.rt.lua;

import java.util.Set;

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
     * The print function name.  This hijacks the "regular" print function and diverts its output
     * to the script's log.  The script's Log is actually backed by slf4j
     */
    String ASSERT_FUNCTION = "assert";

    /**
     * The "package" table.  See the Lua manual for what this is used for.
     */
    String PACKAGE_TABLE = "package";

    /**
     * The "package.searchers" table.  See the Lua manual for what this is used for.
     */
    String PACKAGE_SEARCHERS_TABLE = "searchers";

    /**
     * The lua source file extension "lua"
     */
    String LUA_FILE_EXT = "lua";

    /**
     * The lua loader method "require"
     */
    String REQUIRE = "require";

    /**
     * A field in an object's metatable indicating the type, (eg "object" or "array").
     */
    String MANIFEST_TYPE_METAFIELD = "__namazu_manifest_type";

    /**
     * The lua name of the attributes module
     */
    String ATTRIBUTES_MODULE = "eci.resource.attributes";

    /**
     * The lua name of the attributes module
     */
    String ATTRIBUTES_MODULE_LEGACY = "namazu.resource.attributes";

    /**
     * All attributes modules.
     */
    Set<String> ATTRIBUTES_MODULES = Set.of(ATTRIBUTES_MODULE, ATTRIBUTES_MODULE_LEGACY);

    /**
     * All coroutine module names.
     */
    Set<String> COROUTINE_MODULES = Set.of("eci.coroutine", "namazu.coroutine");

    /**
     * The HTTP Client Modules.
     */
    Set<String> HTTP_CLIENT_MODULES = Set.of("eci.http.client", "namazu.http.client");

    /**
     * The HTTP Status Modules.
     */
    Set<String> HTTP_STATUS_MODULES = Set.of("eci.http.status", "namazu.http.status");

    /**
     * The index detail modules.
     */
    Set<String> INDEX_DETAIL_MODULES = Set.of("eci.index.detail", "namazu.index.detail");

    /**
     * The resource detail modules.
     */
    Set<String> RESOURCE_DETAIL_MODULES = Set.of("eci.resource.detail", "namazu.resource.detail");

    /**
     * The response code modules.
     */
    Set<String> RESPONSE_CODE_MODULES = Set.of("eci.response.code", "namazu.response.code");

    /**
     * The log detail modules.
     */
    Set<String> LOG_DETAIL_MODULES = Set.of("eci.log.detail", "namazu.log.detail");

}
