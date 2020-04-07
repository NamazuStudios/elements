package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.id.ResourceId;

public interface ResourceIndex {

    Revision<Boolean> existsAt(Revision<Void> revision, ResourceId resourceId);

}
