package com.dalstonsemantics.confluence.semantics.cloud.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

/**
 * The only way to deal with dates in Rdf4j is via XMLGregorianCalendar, so we need a converter for LocalDateTime.
 */
public class XMLGregorianCalendarUtil {

    private XMLGregorianCalendarUtil() {
    }

    public static final DateTimeFormatter ISO_DATE_TIME = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .appendFraction(NANO_OF_SECOND, 0, 9, true)
        .optionalStart()
        .appendOffsetId()
        .optionalStart()
        .appendLiteral('[')
        .parseCaseSensitive()
        .appendZoneRegionId()
        .appendLiteral(']')
        .toFormatter();

    public static XMLGregorianCalendar fromLocalDateTime(LocalDateTime ldt) throws DatatypeConfigurationException {
        String isoDateTime = ldt.format(ISO_DATE_TIME);
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(isoDateTime);
        xmlGregorianCalendar.setTimezone(0);
        return xmlGregorianCalendar;
    }

    public static XMLGregorianCalendar fromCalendar(Calendar calendar) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND),
            calendar.get(Calendar.ZONE_OFFSET) / 60000);
    }

    public static XMLGregorianCalendar fromConfluenceDateTime(String datetime) throws DatatypeConfigurationException {
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(datetime);
        xmlGregorianCalendar.setHour(0);
        xmlGregorianCalendar.setMinute(0);
        xmlGregorianCalendar.setSecond(0);
        xmlGregorianCalendar.setMillisecond(0);
        xmlGregorianCalendar.setTimezone(0);
        return xmlGregorianCalendar;
    }

    public static Calendar toCalendar(XMLGregorianCalendar xmlGregorianCalendar){
        return xmlGregorianCalendar.toGregorianCalendar();
    }
}
