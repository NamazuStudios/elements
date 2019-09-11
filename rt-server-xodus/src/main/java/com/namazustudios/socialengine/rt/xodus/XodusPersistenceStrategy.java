package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.ResourceId;

import javax.inject.Inject;

public class XodusPersistenceStrategy implements PersistenceStrategy {

    private XodusResourceService xodusResourceService;

    @Override
    public void persist(final ResourceId resourceId) {
        getXodusResourceService().persist(resourceId);
    }

    public XodusResourceService getXodusResourceService() {
        return xodusResourceService;
    }

    @Inject
    public void setXodusResourceService(XodusResourceService xodusResourceService) {
        this.xodusResourceService = xodusResourceService;
    }

}
