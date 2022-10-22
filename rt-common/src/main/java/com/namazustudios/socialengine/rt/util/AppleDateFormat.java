package com.namazustudios.socialengine.rt.util;

import java.text.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AppleDateFormat extends DateFormat {

    private static final Calendar CALENDAR = new GregorianCalendar();

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");

    public AppleDateFormat() {
        this.numberFormat = NUMBER_FORMAT;
        this.calendar = CALENDAR;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        final String formatted = DATE_TIME_FORMATTER.format(date.toInstant());
        return toAppendTo.append(formatted);
    }

    @Override
    public Date parse(String source) throws ParseException {
        return super.parse(source);
    }

    @Override
    public Date parse(final String source, final ParsePosition pos) {

        final String token = source.substring(pos.getIndex());
        pos.setIndex(source.length());

        final TemporalAccessor accessor = DATE_TIME_FORMATTER.parse(token);
        final Instant instant = Instant.from(accessor);
        return Date.from(instant);

    }

}
