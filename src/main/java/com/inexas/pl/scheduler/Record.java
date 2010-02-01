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
package com.inexas.pl.scheduler;

import java.util.*;

public class Record {
	private final boolean[] minutes, hours, days, dates, months, years;
	private int year, month, date, day, hour, minute, daysInMonth;
	private final static char[] daysInMonthArray = {
	        0, // 1 based
	        31, // January
	        28, // February corrected for leap years on the fly
	        31, // March
	        30, // April
	        31, // May
	        30, // June
	        31, // July
	        31, // August
	        30, // September
	        31, // October
	        30, // November
	        31 // December
	};

	Record(
	        boolean[] minutes,
	        boolean[] hours,
	        boolean[] days,
	        boolean[] dates,
	        boolean[] months,
	        boolean[] years) {
		this.minutes = minutes;
		this.hours = hours;
		this.days = days;
		this.dates = dates;
		this.months = months;
		this.years = years;
	}

	public long getNextRunTime() {
		final Calendar calendar = new GregorianCalendar();
		return getNextRunTime(calendar); 
	}

	long getNextRunTime(Calendar calendar) {
		/*
		 * We use a kind of brute force algorithm here search for valid years
		 * and valid months within that year and valid dates within that month
		 * etc.
		 */
		year = calendar.get(Calendar.YEAR) - 2000; // 2000 = 0
		month = calendar.get(Calendar.MONTH) + 1; // 1=Jan, 2=Feb, ...
		date = calendar.get(Calendar.DAY_OF_MONTH); // 1...
		day = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sunday, 1=Monday, ...
		hour = calendar.get(Calendar.HOUR_OF_DAY); // 0-23 hours
		minute = calendar.get(Calendar.MINUTE); // 0-59 minutes
		setDaysInMonth();
		
		// Make sure there's at least a minute before we fire to avoid tight loops
		nextMinute();
		
		while(year < years.length) {
			if(!years[year]) {
				nextYear();
				continue;
			}
			if(!months[month]) {
				nextMonth();
				continue;
			}
			if(!dates[date] || !days[day]) {
				nextDate();
				continue;
			}
			if(!hours[hour]) {
				nextHour();
				continue;
			}
			if(!minutes[minute]) {
				nextMinute();
				continue;
			}
			break;
		}
		return year == years.length ?
				-1 :
				new GregorianCalendar(year + 2000, month - 1, date, hour, minute).getTimeInMillis();
	}

	private void nextYear() {
		year++;
		month = 1;
		date = 1;
		hour = 0;
		minute = 0;
	}

	private void nextMonth() {
		if(month == 12) {
			nextYear();
		} else {
			month++;
			date = 1;
			hour = 0;
			minute = 0;
		}
		setDaysInMonth();
	}

	private void nextDate() {
		if(date == daysInMonth) {
			nextMonth();
		} else {
			date++;
			hour = 0;
			minute = 0;
		}
		day = (day + 1) % 7;
	}

	private void nextHour() {
		if(hour == 23) {
			nextDate();
		} else {
			hour++;
			minute = 0;
		}
	}

	private void nextMinute() {
		if(minute == 59) {
			nextHour();
		} else {
			minute++;
		}
	}

	private void setDaysInMonth() {
		if(month == 2 && (year % 4) == 0) {
			daysInMonth = 29;
		} else {
			daysInMonth = daysInMonthArray[month];
		}
	}

}
