package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.model.application.Application;
import org.eclipse.jgit.lib.Repository;

/**
 * Created by patricktwohig on 8/1/17.
 */
@FunctionalInterface
public interface ApplicationRepositoryResolver {

    /**
     * Gets the {@link Repository} for the supplied {@link Application}.
     * @param application
     * @return
     */
    Repository resolve(Application application) throws Exception;

}
