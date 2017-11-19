package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Request is a request sent to a particular resource.
 *
 * This ties the {@link RequestHeader} to a specific payload type for
 * the application.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface Request {

    /**
     * Gets the {@link Attributes} of this {@link Request}.
     *
     * @return the {@link Attributes}
     */
    Attributes getAttributes();

    /**
     * Gets ths {@link RequestHeader} object for this particular request.
     *
     * @return the header
     */
    RequestHeader getHeader();

    /**
     * Gets the payload object for this Request.
     *
     * @return the payload
     */
    Object getPayload();

    /**
     * Converts the underlying payload to the requested type, if the conversion is possible.  If the
     * conversion is not possible, then this may throw an exception indicating so.
     *
     * The default implementation of this attempts a simple cast.  If that fails, then the
     * appropriate exception type is raised.
     *
     * @param cls the requested type
     * @param <T> the requested type
     * @throws {@link InvalidConversionException} if the conversion isn't possible
     */
    default <T> T getPayload(final Class<T> cls) {

        final Object payload = getPayload();

        try {
            return cls.cast(payload);
        } catch (ClassCastException ex) {
            throw new InvalidConversionException(ex);
        }

    }

    /**
     * Checks that the request is valid.  A request is considered valid if the header as well
     * as it components are not null.
     *
     * @throws {@link BadRequestException} if the request is not valid
     */
    default void validate() {

        final RequestHeader requestHeader = getHeader();

        if(requestHeader == null) {
            throw new BadRequestException("request header null");
        } else if (requestHeader.getSequence() < 0) {
            throw new BadRequestException("unexpected request sequence " + requestHeader.getSequence());
        } else if (requestHeader.getPath() == null) {
            throw new BadRequestException("invalid path " + requestHeader.getPath());
        } else if (requestHeader.getMethod() == null) {
            throw new BadRequestException("invalid method " + requestHeader.getMethod());
        }

    }

    /**
     * Returns a list of all request parameters with the provided name.  Parameters may be metadata or specific
     * related information, but not tied specifically to the payload as determined by {@link #getPayload()}.
     *
     * @return a {@link List<String>} of parameter names
     */
    List<String> getParameterNames();

    /**
     * Returns a list of all parameter values associated with the supplied parameter name.  If no parameter exists with
     * the provided name, then this must return null.
     *
     * @param parameterName the parameter name
     * @return the {@link List<Object>} of {@link Request} parameters.
     */
    List<Object> getParameters(final String parameterName);

    /**
     * Like {@link #getParameterOrDefault(String, Object)} but returns null instead of a default value.
     *
     * @param parameterName the parameter name
     * @return the first value assocaited with the parameterName or null
     */
    default Object getParameterOrDefault(final String parameterName) {
        return getParameterOrDefault(parameterName, null);
    }

    /**
     * Returns the first parameter obtained, or a default value if no parameter exists.
     *
     * @param parameterName the parameter name
     * @param defaultValue the default value
     * @return the first value returned by {@link #getParameters(String)} or the default value
     */
    default Object getParameterOrDefault(final String parameterName, final Object defaultValue) {
        final List<Object> parameters = getParameters(parameterName);
        return parameters == null || parameters.isEmpty() ? defaultValue : parameters.get(0);
    }

    /**
     * Returns all parameters in the form of a {@link Map<String, List<Object>>}.
     *
     * @return the {@link Map<String, List<Object>>} of parameters and their values.
     */
    default Map<String, List<Object>> getParameterMap() {
        final Map<String, List<Object>> parameterMap = new LinkedHashMap<>();
        getParameterNames().forEach(p -> parameterMap.put(p, getParameters(p)));
        return parameterMap;
    }

}
