package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;

import java.util.Random;
import java.util.UUID;

/**
 * A test model instance.  This has all of the default supported fields.
 *
 * Created by patricktwohig on 5/31/15.
 */
@SearchableIdentity(@SearchableField(name = "id", path = "/id", type = String.class))
@SearchableDocument(
        fields ={
            @SearchableField(name="byteValue", path="/byteValue", type = Byte.class),
            @SearchableField(name="charValue", path="/charValue", type = Character.class),
            @SearchableField(name="shortValue", path="/shortValue", type = Short.class),
            @SearchableField(name="intValue", path="/intValue", type = Integer.class),
            @SearchableField(name="longValue", path="/longValue", type = Long.class),
            @SearchableField(name="floatValue", path="/floatValue", type = Float.class),
            @SearchableField(name="doubleValue", path="/doubleValue", type = Double.class),
            @SearchableField(name="booleanValue", path="/booleanValue", type = Boolean.class),
            @SearchableField(name="stringValue", path="/stringValue", type = String.class),
            @SearchableField(name="textValue", path="/textValue", type = String.class),
            @SearchableField(name="blobValue", path="/blobValue", type = byte[].class, store = Field.Store.YES),
            @SearchableField(name="enumValue", path="/enumValue", type = TestEnum.class),
        }
)
public class TestModel {

    private String id;

    private byte byteValue;

    private char charValue;

    private short shortValue;

    private int intValue;

    private long longValue;

    private float floatValue;

    private double doubleValue;

    private boolean booleanValue;

    private String stringValue;

    private String textValue;

    private byte[] blobValue;

    private TestEnum enumValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public byte[] getBlobValue() {
        return blobValue;
    }

    public void setBlobValue(byte[] blobValue) {
        this.blobValue = blobValue;
    }

    public TestEnum getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue;
    }

    /**
     * Same as {@link #scramble(String)}, only randomly assings an UUID
     * using {@link UUID#randomUUID()}.
     *
     * @return this, scrambled
     */
    public TestModel scramble() {
        return scramble(UUID.randomUUID().toString());
    }

    /**
     * Populates this test model with scrambled data.
     *
     * The rationale behind this is to generate opaque data that can be extrated and
     * tested against later.
     *
     * @param objectId the object's ID to be set using {@link #setId(String)}
     */
    public TestModel scramble(final String objectId) {

        final Random random = new Random();

        setId(objectId);
        setByteValue((byte) (0xff & random.nextInt()));
        setCharValue(Long.toString(random.nextLong()).charAt(0));
        setShortValue((short) random.nextInt());
        setIntValue(random.nextInt());
        setLongValue(random.nextLong());
        setFloatValue(random.nextFloat());
        setDoubleValue(random.nextDouble());
        setBooleanValue(random.nextBoolean());
        setStringValue(Long.toString(random.nextLong()));
        setStringValue(Long.toString(random.nextLong()) + " " + Long.toString(random.nextLong()));
        setTextValue(Long.toString(random.nextLong()) + " " + Long.toString(random.nextLong()));

        final byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        setBlobValue(bytes);

        final TestEnum[] values = TestEnum.values();
        setEnumValue(values[random.nextInt(values.length)]);

        return this;
    }

}
