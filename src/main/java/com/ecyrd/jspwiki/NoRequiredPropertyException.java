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
package com.ecyrd.jspwiki;

/**
 * Marks an erroneus processlab.properties file. Certain properties have been
 * marked as "required", and if you do not provide a good value for a property,
 * you'll see this exception.
 * <P>
 * Check <TT>processlab.properties</TT> for the required properties.
 */
public class NoRequiredPropertyException
        extends WikiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an exception.
	 * 
	 * @param msg
	 *            Message to show
	 * @param key
	 *            The key of the property in question.
	 */
	public NoRequiredPropertyException(String msg, String key) {
		super(msg + ": key=" + key);
	}
}