package dev.getelements.elements.app.serve;

import dev.getelements.elements.common.app.ElementContainerService;

import java.util.List;

public class JettyElementContainerService implements ElementContainerService {

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public List<ContainerRecord> getActiveContainers() {
        return List.of();
    }

}
