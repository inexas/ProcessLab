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
package com.inexas.pl.bi;

import java.io.*;
import java.util.*;
import com.ibm.icu.text.*;

public class PageLogger {
	private static PageLogger instance;
	
	private final static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static String EMPTY_LOG =
	        "!!! DataUpdate log\n" +
	        "This is the DataUpdate log page. Please do not change any text after the '!!! Log' line " +
	        "below. If, for any reason, the file should become messed up you can just delete the page and " +
	        "it will be re-created from scratch\n" +
	        "\n" +
	        "!!! Log\n" +
	        "||Date||Page||User ID||DB||SQL||Result\n";

	private static final long DAY = 24 * 60 * 60 * 1000;
	private static final long WEEK = DAY * 7;
	private static String logFilename;
	private static long lastChecked;
	private final File log;
	private final Date now;
	private final long today;

	public static void initialize(Properties properties) {
		instance = new PageLogger(properties);
	}
	
	public static PageLogger getInstance() {
		return instance;
	}

	private PageLogger(Properties properties) {
		try {
	        if(logFilename == null) {
	        	final String pageHome = (String)properties.get("jspwiki.fileSystemProvider.pageDir");
	        	logFilename = pageHome + "/DataUpdateLog.txt";
	        }

	        now = new Date();
	        final long nowMs = now.getTime();
	        today = nowMs - (nowMs % DAY);

	        // Sanity check: create if necessary and delete if > 100K
	        log = new File(logFilename);
	        if(!log.exists() || log.length() > 100000) {
	        	resetLog();
	        	lastChecked = today;
	        }
	        
	        if(lastChecked == 0 || lastChecked != today) {
	        	removeOldLines();
	        }
        } catch(final Exception e) {
        	// XXX Log error and die silently
        	throw new RuntimeException("Error logging");
        }
    }

	private void removeOldLines() throws Exception {
		/*
		 * We name need to remove old entries from the file. Run through it
		 * keeping an in-memory copy. If we come across old lines then we won't
		 * write them to the buffer. If we see an invalid file we will reset it
		 */
		boolean valid = false;
		boolean changed = false;
		final StringBuilder sb = new StringBuilder();
		final BufferedReader logReader = new BufferedReader(new FileReader(log));
		int state = 0;
		while(true) {
			final String line = logReader.readLine();
			if(line == null) {
				break;
			}
			final String normal = normalize(line);

			boolean write = true;
			switch (state){
			case 0: // Not seen "!!! Log" so just copy...
				if(normal.equals("!!!LOG")) {
					state++;
				}
				break;

			case 1: // Seen "!!! Log", look for table...
				if(normal.startsWith("||DATE||")) {
					state++;
					valid = true;
				}
				break;

			case 2: // Seen table header, delete > week old records...
				// | 2009-10-10 12:34:14 |
				// 01234567890123456789012
				// 1 2
				if(!line.startsWith("| 20") || line.length() < 30) {
					write = false;
				} else {
					final String dateString = line.substring(2, 21);
					final long t = dateFormatter.parse(dateString).getTime();
					final long then = t - (t % DAY);

					if(today - then > WEEK) {
						write = false;
					} else {
						state++;
					}
				}
				break;

			default: // Seen record < week
				break;
			}

			if(write) {
				sb.append(line);
				sb.append('\n');
			} else {
				changed = true;
			}
		}

		if(!valid) {
			resetLog();
		} else if(changed) {
			log.delete();
			final BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));
			logWriter.write(sb.toString());
			logWriter.close();
		}
    }

	public void log(String pageName, String principleName, String db, String sql, String result) {
		try {
	        // | 2009-10-10 12:34:14 | SandBox | John | demo | sql...
	        final BufferedWriter logWriter = new BufferedWriter(new FileWriter(log, true));
	        logWriter.write("| ");
	        logWriter.write(dateFormatter.format(now));
	        logWriter.write(" | ");
	        logWriter.write(pageName);
	        logWriter.write(" | ");
	        logWriter.write(principleName);
	        logWriter.write(" | ");
	        logWriter.write(db);
	        logWriter.write(" | ");
	        logWriter.write(sql);
	        logWriter.write(" | ");
	        logWriter.write(result);
	        logWriter.write('\n');
	        logWriter.close();
        } catch(final IOException e) {
        	throw new RuntimeException("Error logging to page log", e);
        }
    }

	private void resetLog() throws Exception {
		if(log.exists()) {
			log.delete();
		}
		final BufferedWriter logWriter = new BufferedWriter(new FileWriter(log));
		logWriter.write(EMPTY_LOG);
		logWriter.close();
	}

	public String normalize(String string) {
		final StringBuilder result = new StringBuilder();
		final char[] ca = string.toCharArray();
		for(final char c : ca) {
			if(Character.isWhitespace(c)) {
				// Ignore it
			} else if(Character.isLowerCase(c)) {
				result.append(Character.toUpperCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

}
