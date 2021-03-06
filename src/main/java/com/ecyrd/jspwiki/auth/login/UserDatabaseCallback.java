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
package com.ecyrd.jspwiki.auth.login;

import javax.security.auth.callback.Callback;
import com.ecyrd.jspwiki.auth.user.UserDatabase;

/**
 * Callback for requesting and supplying a wiki UserDatabase. This callback is
 * used by LoginModules that need access to a user database for looking up users
 * by id.
 * 
 * @author Andrew Jaquith
 * @since 2.3
 */
public class UserDatabaseCallback implements Callback {

	private UserDatabase m_database;

	/**
	 * Returns the user database object. LoginModules call this method after a
	 * CallbackHandler sets the user database.
	 * 
	 * @return the user database
	 */
	public UserDatabase getUserDatabase() {
		return m_database;
	}

	/**
	 * Sets the user database. CallbackHandler objects call this method..
	 * 
	 * @param database
	 *            the user database
	 */
	public void setUserDatabase(UserDatabase database) {
		this.m_database = database;
	}

}
