package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Field;

/**
 * Adds two more text fields.
 *
 * Created by patricktwohig on 5/31/15.
 */
@SearchableDocument(
        fields = {
                @SearchableField(name="anotherStringValue", path="/anotherStringValue"),
                @SearchableField(name="yetAnotherStringValue", path="/yetAnotherStringValue"),
        }
)
public class TestModelSubclass extends TestModel {

    private String anotherStringValue;

    private String yetAnotherStringValue;

    public String getAnotherStringValue() {
        return anotherStringValue;
    }

    public void setAnotherStringValue(String anotherStringValue) {
        this.anotherStringValue = anotherStringValue;
    }

    public String getYetAnotherStringValue() {
        return yetAnotherStringValue;
    }

    public void setYetAnotherStringValue(String yetAnotherStringValue) {
        this.yetAnotherStringValue = yetAnotherStringValue;
    }

}
