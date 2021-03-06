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
 * This simple class just fulfils the role of a container for searches. It tells
 * the word and whether it is requested or not.
 * 
 */
// FIXME3.0: This should be moved to the search-package, as well as properly
// rewritten as a bean.
public class QueryItem {
	/** The word is required to be in the pages */
	public static final int REQUIRED = 1;

	/** The word may NOT be in the pages */
	public static final int FORBIDDEN = -1;

	/**
	 * The word should be in the pages, but the search engine may use its own
	 * discretion.
	 */
	public static final int REQUESTED = 0;

	/** The word that is being searched */
	public String word;

	/**
	 * The type of the word. See above for types. The default is REQUESTED.
	 */
	public int type = REQUESTED;
}
