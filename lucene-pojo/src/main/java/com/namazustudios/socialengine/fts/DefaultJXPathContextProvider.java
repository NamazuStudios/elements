package com.namazustudios.socialengine.fts;

import org.apache.commons.jxpath.JXPathContext;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class DefaultJXPathContextProvider implements JXPathContextProvider {

    private static final DefaultJXPathContextProvider INSTANCE = new DefaultJXPathContextProvider();

    private DefaultJXPathContextProvider() {}

    @Override
    public JXPathContext get(Object rootObject) {
        final JXPathContext jxPathContext = JXPathContext.newContext(rootObject);
        jxPathContext.getVariables().declareVariable(ROOT_OBJECT_NAME, rootObject);
        return jxPathContext;
    }

    public static DefaultJXPathContextProvider getInstance() {
        return INSTANCE;
    }

}
