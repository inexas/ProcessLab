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

import java.security.*;

public abstract class Member {
	protected final Long key;
	private String name, description;
	protected Member parent;
	private Principal owner;

	public Member(Long key, String name) {
		assert key != null;
		this.key = key;
		this.name = name;
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
		// todo should I check here or at a higher level
		assert Directory.isValidName(name);
    	this.name = name;
    }

	public String getDescription() {
    	return description;
    }

	public void setDescription(String description) {
    	this.description = description;
    }

	public Long getKey() {
    	return key;
    }

	/**
	 * Set the owner. The owner may be null implying that the
	 * ownership is inherited.
	 * 
	 * @param owner
	 */
	public void setOwner(Principal owner) {
		assert owner != null || parent != null : "Root must have a non-null owner";
	    this.owner = owner;
    }

	public Principal getOwner() {
	    return owner == null ? parent.getOwner() : owner;
    }

	public void setParent(Member parent) {
		assert parent != null || key.equals(Context.ROOT_KEY) : "Only the Root context can have a null parent";
	    this.parent = parent;
    }

	public Member getParent() {
	    return parent;
    }
	
	public String getPathname() {
		final StringBuilder sb = new StringBuilder();
		getPathname(sb);
		return sb.toString();
	}

	private void getPathname(StringBuilder sb) {
		if(parent != null) {
			getPathname(sb);
		}
		sb.append(name);
		if(this instanceof Context) {
			sb.append('/');
		}
    }
	
	public void delete() {
		assert parent != null : "Already deleted or this is root";
		parent = null;
	}
}
