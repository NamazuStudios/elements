package com.namazustudios.socialengine.rt.http;

import com.google.common.base.Splitter;
import com.google.common.net.MediaType;
import com.namazustudios.socialengine.rt.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AcceptableContentType implements Comparable<AcceptableContentType> {

    private final MediaType mediaType;

    private final double quality;

    public static List<AcceptableContentType> parseHeader(final String headerValue) {

        final List<AcceptableContentType> acceptableContentTypeList = new ArrayList<>();

        for(final String value : Splitter.on(",").trimResults().split(headerValue)) {
            final AcceptableContentType acceptableContentType = parse(value);
            acceptableContentTypeList.add(acceptableContentType);
        }

        return acceptableContentTypeList;

    }

    public static AcceptableContentType parse(final String accepts) {

        final Iterator<String> acceptsIterator = Splitter.on(";").trimResults().split(accepts).iterator();

        if (!acceptsIterator.hasNext()) {
            throw new BadRequestException("invalid MIME type " + accepts);
        }

        final String mediaTypeString = acceptsIterator.next();
        final String qualityString = acceptsIterator.hasNext() ? acceptsIterator.next() : "1.0";

        if (acceptsIterator.hasNext()) {
            throw new BadRequestException("Invalid AcceptableContentType header specification " + accepts);
        }

        try {
            final double quality = Double.parseDouble(qualityString);
            final MediaType mediaType = MediaType.parse(mediaTypeString);
            return new AcceptableContentType(mediaType, quality);
        } catch (NumberFormatException nfe) {
            throw new BadRequestException(nfe);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }

    }

    public AcceptableContentType(final MediaType mediaType, final double quality) {

        if (quality < 0 || quality > 1) {
            throw new BadRequestException("quality out of range");
        }

        this.mediaType = mediaType;
        this.quality = quality;

    }

    public double getQuality() {
        return quality;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public int compareTo(AcceptableContentType o) {
        return quality >  o.quality ? -1 :
               quality == o.quality ?  0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcceptableContentType)) return false;

        AcceptableContentType that = (AcceptableContentType) o;

        if (Double.compare(that.getQuality(), getQuality()) != 0) return false;
        return getMediaType().equals(that.getMediaType());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getQuality());
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + getMediaType().hashCode();
        return result;
    }
}
