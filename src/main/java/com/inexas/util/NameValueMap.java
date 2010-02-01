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

import java.io.*;
import java.util.*;
import com.ecyrd.jspwiki.*;

/**
 * This class reads a list of name value pairs from a wiki page.
 * 
 * todo Replace this with a serious implementation once we've got CRUD handling, see below
 * 
 */
public class NameValueMap {
	private final Map<String, String> map = new HashMap<String, String>();
	
	public NameValueMap(WikiEngine engine, String pageName) {

		final String pageHome = (String)engine.getWikiProperties().get("jspwiki.fileSystemProvider.pageDir");
		final String filename = pageHome + '/' + pageName.replace(' ', '+') + ".txt";
		
		try {
			final File file = new File(filename);
			if(file.exists()) {
				final BufferedReader in = new BufferedReader(new FileReader(filename));
				String line;
				boolean inConfig = false;
				while((line = in.readLine()) != null) {
					// Ignore comments
					final int comment = line.indexOf("//");
					if(comment >= 0) {
						line = line.substring(0, comment);
						
					}
					
					// Ignore empty lines...
					line = line.trim();
					if(line.length() == 0) {
						continue;
					}
					
					if(inConfig) {
						if(line.startsWith("}}}")) {
							inConfig = false;
							continue;
						}
					} else { // Not inConfig
						if(line.startsWith("{{{SD")) {
							inConfig = true;
						}
						continue;
					}
					final int equals = line.indexOf('=');
					if(equals < 0) {
						continue;
					}
					
					final String key = line.substring(0, equals).trim();
					final String value = line.substring(equals + 1).trim();
					map.put(key, value);
				}
				in.close();
			}
		} catch(final IOException e) {
			throw new RuntimeException("Cannot read: " + filename, e);
		}
	}

	public Map<String, String> getMap() {
		return map;
    }

}
