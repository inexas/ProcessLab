/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.util;

import java.text.*;
import java.util.*;

public class DateUtilities {
	private final static DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private final static DateFormat dateOnlyFormatter = new SimpleDateFormat("yyyy/MM/dd");
	private final static DateFormat timeOnlyFormatter = new SimpleDateFormat("HH:mm:ss");
	private final static String DATE_MESSAGE =	//
		"Date should be in format 'yyyy/MM/dd HH:mm:ss', you may have " +	//
		"date only, time only or date and time all units in descending " +	//
		"order: ";

	public static String toDateString(Date date) {
		return dateOnlyFormatter.format(date);
	}

	public static String toTimeString(Date date) {
		return timeOnlyFormatter.format(date);
	}

	public static String toDateTimeString(Date date) {
		return dateTimeFormatter.format(date);
	}

	public static Date toDate(String string) {
		int whichFormatter = 0;
		if(string.indexOf('/') > 0) {
			whichFormatter |= 0x01;
		}
		if(string.indexOf(':') > 0) {
			whichFormatter |= 0x02;
		}
		final DateFormat formatter;
		switch(whichFormatter) {
		case 1:
			formatter = dateOnlyFormatter;
			break;
		case 2:
			formatter = timeOnlyFormatter;
			break;
		case 3:
			formatter = dateTimeFormatter;
			break;
		default:
			throw new RuntimeException(DATE_MESSAGE + string);
		}
		try {
	        final Date result = formatter.parse(string);
	        return result;
        } catch(ParseException e) {
        	throw new RuntimeException(DATE_MESSAGE + string, e);
        }
    }
	
	
	/**
	 * Convert milliseconds to a human readable duration
	 */
	public static String humanReadableDuration(long ms) {
		final StringBuilder sb = new StringBuilder();
		if(ms < 1999) { // 0.1999 ms: 1 second
			sb.append("1 second");
		} else if(ms < 60 * 1000) { // 2-59 secs: x seconds
			sb.append(ms / 1000);
			sb.append(" seconds");
		} else if(ms < 60 * 60  * 1000) { // 1-59 mins: x minutes
			final long minutes = ms / (60 * 1000);
			sb.append(minutes);
			sb.append(minutes > 1 ? " minutes" : " minute");
		} else if(ms < 24 * 60 * 60 * 1000) { // 1-23 hours: x hours
			final long hours = ms / (60 * 60 * 1000);
			sb.append(hours);
			sb.append(hours > 1 ? " hours" : " hour");
		} else { // 1.* days = x days
			final long days = ms / (24 * 60 * 60 * 1000);
			sb.append(days);
			sb.append(days > 1 ? " days" : " day");
		}
		return sb.toString();
	}

}
