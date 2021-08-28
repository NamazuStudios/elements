package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;
import com.namazustudios.socialengine.rt.annotation.Intrinsic;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LDocStubProcessorIntrinsic implements DocProcessor<LDocRootStubModule> {

    private final Intrinsic intrinsic;

    public LDocStubProcessorIntrinsic(final Intrinsic intrinsic) {
        this.intrinsic = intrinsic;
    }

    @Override
    public List<LDocRootStubModule> process() {

        final var stubs = ModuleDefinitionMetadata.streamFrom(intrinsic)
            .map(LDocRootStubModule::new)
            .collect(toList());

        for (var stub : stubs) {

            final var header = stub.getHeader();

            header.setSummary(intrinsic.summary());
            header.setDescription(intrinsic.description());
            Stream.of(intrinsic.authors()).forEach(header::addAuthor);

            for (var constant : intrinsic.constants()) {
                final var field = stub.addConstant(constant.value());
                field.setType(constant.type());
                field.setDescription(constant.description());
                field.setConstantValue(constant.literal());
            }

            for (var method : intrinsic.methods()) {

                final var m = stub.addFunction(method.value());
                m.setSummary(method.summary());
                m.setDescription(method.description());

                for (var ret : method.returns()) m.addReturnValue(ret.type(), ret.comment());
                for (var param : method.parameters()) m.addParameter(param.value(), param.type(), param.comment());

            }

        }

        return stubs;

    }

}
