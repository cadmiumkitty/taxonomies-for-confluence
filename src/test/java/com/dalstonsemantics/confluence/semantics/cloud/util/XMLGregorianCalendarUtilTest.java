package com.dalstonsemantics.confluence.semantics.cloud.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

public class XMLGregorianCalendarUtilTest {
    
    @Test
    public void shouldCreateXMLGregorianCalendarFromEpochLocalDateTime() throws Exception {

        // Ignore nanos for the tests, we just want to make sure that seconds are converted correctly for when
        // value is missing
        LocalDateTime ldtNow = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        XMLGregorianCalendar xmlGregorianCalendar = XMLGregorianCalendarUtil.fromLocalDateTime(ldtNow);

        assertEquals(2021, xmlGregorianCalendar.getYear());
        assertEquals(1, xmlGregorianCalendar.getMonth());
        assertEquals(1, xmlGregorianCalendar.getDay());
        assertEquals(0, xmlGregorianCalendar.getHour());
        assertEquals(0, xmlGregorianCalendar.getMinute());
        assertEquals(0, xmlGregorianCalendar.getSecond());
        assertEquals(0, xmlGregorianCalendar.getTimezone());
    }

    @Test
    public void shouldCreateXMLGregorianCalendarFromCalendar() throws Exception {

        // Calendar is using 0-based months, this is 1-Jan-2021
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, 0, 1, 10, 20, 30);
        calendar.set(Calendar.MILLISECOND, 40);
        calendar.set(Calendar.ZONE_OFFSET, 60000 * 5);

        XMLGregorianCalendar xmlGregorianCalendar = XMLGregorianCalendarUtil.fromCalendar(calendar);

        // XMLGregorianCalendar is using 1-based months, this is 1-Jan-2021
        assertEquals(2021, xmlGregorianCalendar.getYear());
        assertEquals(1, xmlGregorianCalendar.getMonth());
        assertEquals(1, xmlGregorianCalendar.getDay());
        assertEquals(10, xmlGregorianCalendar.getHour());
        assertEquals(20, xmlGregorianCalendar.getMinute());
        assertEquals(30, xmlGregorianCalendar.getSecond());
        assertEquals(40, xmlGregorianCalendar.getMillisecond());
        assertEquals(5, xmlGregorianCalendar.getTimezone());
    }

    @Test
    public void shouldCreateCalendarFromXMLGregorianCalendar() throws Exception {

        // LocalDateTime is using 1-based months, this is 1-Jan-2021
        LocalDateTime ldtNow = LocalDateTime.of(2021, 1, 1, 10, 20, 30, 40000000);
        XMLGregorianCalendar xmlGregorianCalendar = XMLGregorianCalendarUtil.fromLocalDateTime(ldtNow);

        Calendar calendar = XMLGregorianCalendarUtil.toCalendar(xmlGregorianCalendar);

        // Calendar is using 0-based months, this is 1-Jan-2021
        assertEquals(2021, calendar.get(Calendar.YEAR));
        assertEquals(0, calendar.get(Calendar.MONTH));
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, calendar.get(Calendar.MINUTE));
        assertEquals(30, calendar.get(Calendar.SECOND));
        assertEquals(40, calendar.get(Calendar.MILLISECOND));
        assertEquals(0, calendar.get(Calendar.ZONE_OFFSET) / 60000);
    }

    @Test
    public void shouldCreateXMLGregorianCalendarFromConfluenceDateTime() throws Exception {

        XMLGregorianCalendar xmlGregorianCalendar = XMLGregorianCalendarUtil.fromConfluenceDateTime("2022-12-27");

        assertEquals(2022, xmlGregorianCalendar.getYear());
        assertEquals(12, xmlGregorianCalendar.getMonth());
        assertEquals(27, xmlGregorianCalendar.getDay());
        assertEquals(0, xmlGregorianCalendar.getHour());
        assertEquals(0, xmlGregorianCalendar.getMinute());
        assertEquals(0, xmlGregorianCalendar.getSecond());
        assertEquals(0, xmlGregorianCalendar.getMillisecond());
        assertEquals(0, xmlGregorianCalendar.getTimezone());
    }
}