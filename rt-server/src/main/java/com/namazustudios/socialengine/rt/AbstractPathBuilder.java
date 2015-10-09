package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 10/8/15.
 */
public abstract class AbstractPathBuilder<NextT> implements PathBuilder<NextT> {

    @Override
    public NextT atPath(String path) {
        return atPath(new Path(path));
    }

}
