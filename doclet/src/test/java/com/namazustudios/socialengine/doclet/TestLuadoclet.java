package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.doclet.lua.LDocStubDoclet;
import org.testng.annotations.Test;

import javax.tools.ToolProvider;

import static org.testng.Assert.assertEquals;

public class TestLuadoclet {

    @Test
    public void testGenerate() {

        final var result = ToolProvider.getSystemDocumentationTool().run(
            System.in, System.out, System.err,
            "-sourcepath", "doclet/src/test/java",
            "-subpackages", "com.namazustudios",
//            "-d", "target/lua-doclet",
            "-doclet", LDocStubDoclet.class.getName()
        );

        assertEquals(0, result);

    }

}
