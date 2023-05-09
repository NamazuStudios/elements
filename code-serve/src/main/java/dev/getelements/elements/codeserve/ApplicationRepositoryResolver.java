package dev.getelements.elements.codeserve;

import dev.getelements.elements.model.application.Application;
import org.eclipse.jgit.lib.Repository;

import java.util.function.Consumer;

/**
 * Created by patricktwohig on 8/1/17.
 */
@FunctionalInterface
public interface ApplicationRepositoryResolver {

    /**
     * Gets the {@link Repository} for the supplied {@link Application}.  This simply returns
     * the instance of {@link Repository}.  It safe ot assume this method will be called after
     * the necessary security checks, therefore any security checking in this method would
     * be redundant.
     *
     * If no {@link Repository} exists, this must create the repository.
     *
     * @param application the {@link Application}
     * @param onCreate
     * @return the {@link Repository}, never null
     *
     */
    Repository resolve(Application application, Consumer<Repository> onCreate) throws Exception;

}
