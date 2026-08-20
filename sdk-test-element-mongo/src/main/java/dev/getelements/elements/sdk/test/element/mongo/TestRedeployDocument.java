package dev.getelements.elements.sdk.test.element.mongo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * A minimal Morphia entity, deliberately shaped like the real-world entity from issue #40
 * ({@code MeteringBatchDocument}): a non-{@code ObjectId} {@code String} id, no
 * {@code useDiscriminator = false} override.
 */
@Entity("test_redeploy_document")
public class TestRedeployDocument {

    @Id
    private String id;

    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
