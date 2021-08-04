package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.rt.annotation.Intrinsic;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LDocStubProcessorIntrinsic implements DocProcessor<LDocRootStubModule> {

    private final DocContext docContext;

    private final Intrinsic intrinsic;

    private final TypeElement typeElement;

    public LDocStubProcessorIntrinsic(final DocContext docContext,
                                      final Intrinsic intrinsic,
                                      final TypeElement typeElement) {
        this.docContext = docContext;
        this.intrinsic = intrinsic;
        this.typeElement = typeElement;
    }

    @Override
    public List<LDocRootStubModule> process() {

        final var stubs = Arrays.stream(intrinsic.value())
            .map(LDocRootStubModule::new)
            .collect(toList());

        for (var stub : stubs) {

            final var header = stub.getHeader();

            header.setSummary(intrinsic.summary());
            header.setDescription(intrinsic.description());
            Stream.of(intrinsic.authors()).forEach(header::addAuthor);

            for (var constant : intrinsic.constants()) {
                final var field = header.addField(constant.value(), constant.sourceCaseFormat());
                field.setType(constant.type());
                field.setDescription(constant.description());
                field.setConstantValue(constant.literal());
            }

            for (var method : intrinsic.methods()) {
                final var m = stub.addMethod(method.value());
                for (var ret : method.returns()) m.addReturnValue(ret.type(), ret.comment());
                for (var param : method.parameters()) m.addParameter(param.value(), param.type(), param.comment());
            }

        }

        return stubs;

    }

}
