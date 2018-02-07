package com.namazustudios.socialengine.rt.http;

import com.google.common.base.Splitter;
import com.google.common.net.MediaType;
import com.namazustudios.socialengine.rt.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Accept implements Comparable<Accept> {

    private final MediaType mediaType;

    private final double quality;

    public static List<Accept> parseHeader(final String headerValue) {

        final List<Accept> acceptList = new ArrayList<>();

        for(final String value : Splitter.on(",").trimResults().split(headerValue)) {
            final Accept accept = parse(value);
            acceptList.add(accept);
        }

        return acceptList;

    }

    public static Accept parse(final String accepts) {

        final Iterator<String> acceptsIterator = Splitter.on(";").trimResults().split(accepts).iterator();

        if (!acceptsIterator.hasNext()) {
            throw new BadRequestException("invalid MIME type " + accepts);
        }

        final String mediaTypeString = acceptsIterator.next();
        final String qualityString = acceptsIterator.hasNext() ? acceptsIterator.next() : "q=1.0";

        if (acceptsIterator.hasNext()) {
            throw new BadRequestException("Invalid Accept header specification " + accepts);
        }

        final MediaType mediaType;

        try {
            mediaType = MediaType.parse(mediaTypeString);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }

        final double quality = parseQualityString(qualityString);
        return new Accept(mediaType, quality);

    }

    private static double parseQualityString(final String qualityString) {

        final Iterator<String> qualityStringIterator = Splitter.on('=')
            .trimResults()
            .omitEmptyStrings()
            .split(qualityString)
            .iterator();

        if (!qualityStringIterator.hasNext() ||
            !"q".equals(qualityStringIterator.next()) ||
            !qualityStringIterator.hasNext()) {
            throw new BadRequestException("Invalid quality string.");
        }

        final String qualityValueString = qualityStringIterator.next();

        if (qualityStringIterator.hasNext()) {
            throw new BadRequestException("Invalid quality string.");
        }

        try {
            return Double.parseDouble(qualityValueString);
        } catch (NumberFormatException nfe) {
            throw new BadRequestException(nfe);
        }

    }

    public Accept(final MediaType mediaType, final double quality) {

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
    public int compareTo(Accept o) {
        return quality >  o.quality ? -1 :
               quality == o.quality ?  0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Accept)) return false;

        Accept that = (Accept) o;

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
