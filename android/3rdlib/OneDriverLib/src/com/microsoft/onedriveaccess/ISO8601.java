package com.microsoft.onedriveaccess;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for handling ISO 8601 strings of the following format: "2008-03-01T13:00:00".
 */
final class ISO8601 {
	
	private static final String DATE_FORMAT_MATCHER = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{4,}Z$";
	
	private static final String DATE_FORMAT_MATCHER_2 = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{4,}[+-]\\d{2}:\\d{2}$";
	
	private static final String ZONE_FORMAT_MATCHER = "[+-]\\d{2}:\\d{2}$";
	
    /**
     * The ISO8601 date format string, see https://en.wikipedia.org/wiki/ISO_8601
     * Modified slightly to match the OData response from OneDrive
     */
    public static final String DATE_FORMAT_MILLIS_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Same as DATE_FORMAT_MILLIS_STRING without the millis
     */
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * Default constructor
     */
    private ISO8601() {
    }

    /**
     * Transform Date to ISO 8601 string.
     * @param date to convert
     * @return the date as an ISO 8601 string
     */
    public static String fromDate(final Date date) {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT_MILLIS_STRING);
        final DateTime dateTime = new DateTime(date);
        return formatter.print(dateTime);
    }

    /**
     * Transform ISO 8601 string to a Date.
     * @param iso8601string to convert
     * @return the date
     * @exception ParseException If the date could not be parsed
     */
    public static Date toDate(final String iso8601string)
            throws ParseException {
        try {
        	String str = iso8601string;
        	Matcher matcher = Pattern.compile(DATE_FORMAT_MATCHER).matcher(iso8601string);
        	if(matcher.find()) {
        		str = matcher.group();
        		str = str.substring(0, 23) + 'Z';
        	} else {
        		matcher = Pattern.compile(DATE_FORMAT_MATCHER_2).matcher(iso8601string);
        		if(matcher.find()) {
        			str = matcher.group();
        			matcher = Pattern.compile(ZONE_FORMAT_MATCHER).matcher(iso8601string);
        			
        			String zone = "+00:00";
        			if(matcher.find()) {
        				zone = matcher.group();
        			}
        			
            		str = str.substring(0, 23) + zone;
        		}
        	}
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT_MILLIS_STRING);
            return formatter.parseDateTime(str).toDate();
        } catch (final IllegalArgumentException ex) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
            return formatter.parseDateTime(iso8601string).toDate();
        }
    }
}
