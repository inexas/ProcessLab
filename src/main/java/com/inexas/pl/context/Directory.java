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
package com.inexas.pl.context;

public class Directory {
	private final Context root;

	public Directory() {
		root = new Context(Context.ROOT_KEY, "");
    }
	
	public Context getRoot() {
	    return root;
    }
	
	public static boolean isValidName(String candidate) {
		// LETTER [[ALPHA | LETTER | '_'])+
		boolean result;
		if(candidate == null) {
			result = false;
		} else {
			final char[] ca = candidate.toCharArray();
			final int length = ca.length;
			if(length == 0) {
				result = false;
			} else {
				if(!Character.isLetter(ca[0])) {
					result = false;
				} else {
					result = true;
					for(int i = 1; i < length; i++) {
						final char c = ca[i];
						if(Character.isLetterOrDigit(c) && c != '_') {
							result = false;
							break;
						}
					}
				}
			}
		}
		return result;
	}
}
