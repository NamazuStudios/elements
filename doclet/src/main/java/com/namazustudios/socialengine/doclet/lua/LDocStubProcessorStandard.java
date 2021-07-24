package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class LDocStubProcessorStandard implements LDocProcessor<LDocStubClass> {

    private final DocContext docContext;

    private final TypeElement typeElement;

    public LDocStubProcessorStandard(final DocContext docContext, final TypeElement typeElement) {
        this.docContext = docContext;
        this.typeElement = typeElement;
    }

    @Override
    public List<LDocStubClass> process() {
        return null;
    }

}
