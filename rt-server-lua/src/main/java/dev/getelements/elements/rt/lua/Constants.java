package dev.getelements.elements.rt.lua;

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
    String ATTRIBUTES_MODULE = "namazu.resource.attributes";

}
