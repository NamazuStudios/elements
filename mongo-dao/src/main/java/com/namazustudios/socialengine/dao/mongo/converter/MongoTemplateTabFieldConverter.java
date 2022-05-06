package com.namazustudios.socialengine.dao.mongo.converter;

import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoTemplateTabField;
import com.namazustudios.socialengine.model.schema.template.TemplateTabField;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

import java.util.HashMap;
import java.util.Map;

public class MongoTemplateTabFieldConverter implements CustomConverter {

    @Override
    public Object convert(final Object existingDestinationFieldValue, final Object sourceFieldValue,
                          final Class<?> destinationClass, final Class<?> sourceClass) {
        if (sourceClass == MongoTemplateTabField.class && destinationClass == MongoTemplateTabField.class) {
            return sourceFieldValue;
        } else if (sourceFieldValue instanceof Map) {
            try{
                Map<String, MongoTemplateTabField> fields =  (Map<String, MongoTemplateTabField>) sourceFieldValue;
                Map<String, TemplateTabField> returnFields =  new HashMap<>();
                for (Map.Entry<String, MongoTemplateTabField> entry : fields.entrySet()){
                    MongoTemplateTabField value = entry.getValue();
                    TemplateTabField result = new TemplateTabField();
                    result.setName(value.getName());
                    result.setFieldType(value.getFieldType());
                    result.setDisplayName(value.getDisplayName());
                    result.setRequired(value.getRequired());
                    result.setDefaultValue(value.getDefaultValue());
                    result.setPlaceHolder(value.getPlaceHolder());
                    returnFields.put(entry.getKey(), result);
                }
                return returnFields;
            }catch (ClassCastException e){
                Map<String, TemplateTabField> fields =  (Map<String, TemplateTabField>) sourceFieldValue;
                Map<String, MongoTemplateTabField> returnFields =  new HashMap<>();
                for (Map.Entry<String, TemplateTabField> entry : fields.entrySet()){
                    TemplateTabField value = entry.getValue();
                    MongoTemplateTabField result = new MongoTemplateTabField();
                    result.setName(value.getName());
                    result.setFieldType(value.getFieldType());
                    result.setDisplayName(value.getDisplayName());
                    result.setRequired(value.getRequired());
                    result.setDefaultValue(value.getDefaultValue());
                    result.setPlaceHolder(value.getPlaceHolder());
                    returnFields.put(entry.getKey(), result);
                }
                return returnFields;
            }
        } else {
            throw new MappingException("No conversion exists betweeen " + sourceClass + " and " + destinationClass);
        }
    }

}
