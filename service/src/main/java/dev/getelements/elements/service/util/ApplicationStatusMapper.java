package dev.getelements.elements.service.util;

import dev.getelements.elements.common.app.ApplicationDeploymentService.DeploymentRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.application.ApplicationStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.system.ElementMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ApplicationStatusMapper extends MapperRegistry.Mapper<DeploymentRecord, ApplicationStatus> {

    @Override
    @Mapping(source = "applicationElementRecord.elements", target = "elements")
    ApplicationStatus forward(DeploymentRecord source);

    default List<ElementMetadata> mapElementMetadata(final List<Element> source) {
        return source == null
                ? List.of()
                : source.stream().map(ElementMetadata::from).toList();
    }

}
