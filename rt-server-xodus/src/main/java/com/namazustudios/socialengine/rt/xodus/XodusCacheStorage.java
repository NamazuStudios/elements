package com.namazustudios.socialengine.rt.xodus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class XodusCacheStorage {

    private final Map<XodusCacheKey, XodusResource> resourceIdResourceMap = new ConcurrentHashMap<>();

    public Map<XodusCacheKey, XodusResource> getResourceIdResourceMap() {
        return resourceIdResourceMap;
    }

}
