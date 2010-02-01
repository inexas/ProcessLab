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

import java.util.*;

public class Context extends Member {
	public static final Long ROOT_KEY = new Long(1); 
	// !todo Handle circularity
	private final Map<Long, Member> children = new LinkedHashMap<Long, Member>();
	
	public Context(Long key, String name) {
		super(key, name);
    }

	/**
	 * @param child to add
	 * @return true if child added, false if it was already a member
	 */
	public boolean add(Member child) {
		final boolean result;
		final Long childKey = child.key;
		if(children.containsKey(childKey)) {
			result = false;
		} else {
			children.put(childKey, child);
			result = true;
		}
		return result;
	}
	
	public Member remove(Member child) {
		assert child.parent != this : "You probably mean Member.setParent() or Member.delete()";
		return children.remove(child.key);
	}
	
}
