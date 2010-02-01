/*
  Copyright (c) Inexas 2010

  Modifications licensed under the Inexas Software License V1.0. You
  may not use this file except in compliance with the License.

  The License is available at: http://www.inexas.com/ISL-V1.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  The original file and contents are licensed under a separate license:
  see below.
*/
/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package com.ecyrd.jspwiki.auth;

import java.security.Principal;

/**
 * Immutable Principal that represents a Group. GroupPrincipals are injected
 * into a Subject's principal list at the time of authentication (login), and
 * serve as proxies for Group objects for the purposes of making Java 2 security
 * policy decisions. We add GroupPrincipals instead of the actual Groups because
 * calling classes should never be able to obtain a mutable object (Group
 * memberships can be changed by callers). Administrators who wish to grant
 * privileges to specific wiki groups via the security policy file should always
 * specify principals of type GroupPrincipal.
 * 
 * @see com.ecyrd.jspwiki.auth.authorize.Group
 * @author Andrew Jaquith
 * @since 2.3.79
 */
public final class GroupPrincipal implements Principal {
	private final String m_name;

	/**
	 * Constructs a new GroupPrincipal object with a supplied name.
	 * 
	 * @param group
	 *            the wiki group; cannot be <code>null</code>
	 */
	public GroupPrincipal(String group) {
		if(group == null) {
			throw new IllegalArgumentException("Group parameter cannot be null.");
		}
		m_name = group;
	}

	/**
	 * Returns the name of the group principal.
	 * 
	 * @return the name
	 * @see java.security.Principal#getName()
	 */
	public final String getName() {
		return m_name;
	}

	/**
	 * Two GroupPrincipals are equal if their names are equal.
	 * 
	 * @param obj
	 *            the object to compare
	 * @return the result of the equality test
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
    public final boolean equals(Object obj) {
		if(!(obj instanceof GroupPrincipal)) {
			return false;
		}
		GroupPrincipal p = (GroupPrincipal)obj;
		return p.m_name.equals(m_name);
	}

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hash code
	 * @see java.lang.Object#hashCode()
	 */
	@Override
    public final int hashCode() {
		return m_name.hashCode();
	}

	/**
	 * Returns a string representation of this object.
	 * 
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	@Override
    public final String toString() {
		return "[GroupPrincipal " + m_name + "]";
	}

}
