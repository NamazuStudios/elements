package com.namazustudios.socialengine.rest.support;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import java.util.Date;

/**
 * Created by patricktwohig on 7/20/17.
 */
@Provider
public class ISODateParamConverter implements ParamConverter<Date> {

    @Override
    public Date fromString(String value) {
        return null;
    }

    @Override
    public String toString(Date value) {
        return null;
    }

}
