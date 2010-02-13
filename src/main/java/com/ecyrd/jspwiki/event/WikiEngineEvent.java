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

package com.ecyrd.jspwiki.event;

import com.ecyrd.jspwiki.WikiEngine;

/**
 * WikiEngineEvent indicates a change in the state of the WikiEngine.
 * 
 * @author Murray Altheim
 * @author Andrew Jaquith
 * @see com.ecyrd.jspwiki.event.WikiEvent
 * @since 2.4.20
 */
public class WikiEngineEvent extends WikiEvent {
	private static final long serialVersionUID = 1829433967558773970L;

	/**
	 * Indicates a WikiEngine initialization event, fired as the wiki service is
	 * being initialized (in progress).
	 */
	public static final int INITIALIZING = -1;

	/**
	 * Indicates a WikiEngine initialized event, fired after the wiki service is
	 * fully available.
	 */
	public static final int INITIALIZED = 0;

	/**
	 * Indicates a WikiEngine closing event, fired as a signal that the wiki
	 * service is shutting down.
	 */
	public static final int SHUTDOWN = 1;

	/**
	 * Indicates a WikiEngine stopped event, fired after halting the wiki
	 * service. A WikiEngine in this state is not expected to provide further
	 * services.
	 */
	public static final int STOPPED = 2;

	private WikiEngine m_engine;

	// ............

	/**
	 * Constructs an instance of this event.
	 * 
	 * @param eventSource
	 *            the Object that is the source of the event, which <b>must</b>
	 *            be the WikiEngine. If it is not, this method thows a
	 *            ClassCastException
	 * @param type
	 *            the event type
	 */
	public WikiEngineEvent(Object eventSource, int type) {
		super(eventSource, type);
		m_engine = (WikiEngine)eventSource;
	}

	/**
	 * Sets the type of this event.
	 * 
	 * @param type
	 *            the type of this WikiEngineEvent.
	 */
	@Override
    protected void setType(int type) {
		if(type >= INITIALIZING && type <= STOPPED) {
			super.setType(type);
		} else {
			super.setType(ERROR);
		}
	}

	/**
	 * Returns the WikiEngine that spawned this event.
	 * 
	 * @return the WikiEngine that spawned this event.
	 */
	public WikiEngine getEngine() {
		return m_engine;
	}

	/**
	 * Returns the WikiEngine that spawned this event.
	 * 
	 * @return the WikiEngine that spawned this event.
	 * @deprecated use {@link #getEngine()} instead.
	 */
	public WikiEngine getWikiEngine() {
		return m_engine;
	}

	/**
	 * Returns <code>true</code> if the int value is a WikiPageEvent type.
	 * 
	 * @param type
	 *            the event type
	 * @return the result
	 */
	public static boolean isValidType(int type) {
		return type >= INITIALIZING && type <= STOPPED;
	}

	/**
	 * Returns a textual representation of the event type.
	 * 
	 * @return a String representation of the type
	 */
	@Override
    public final String eventName() {
		switch (getType()){
		case INITIALIZING:
			return "INITIALIZING";
		case INITIALIZED:
			return "INITIALIZED";
		case SHUTDOWN:
			return "SHUTDOWN";
		case STOPPED:
			return "STOPPED";
		default:
			return super.eventName();
		}
	}

	/**
	 * Returns a human-readable description of the event type.
	 * 
	 * @return a String description of the type
	 */
	@Override
    public final String getTypeDescription() {
		switch (getType()){
		case INITIALIZING:
			return "wiki engine initializing";
		case INITIALIZED:
			return "wiki engine initialized";
		case SHUTDOWN:
			return "wiki engine shutting down";
		case STOPPED:
			return "wiki engine stopped";
		default:
			return super.getTypeDescription();
		}
	}

} // end class com.ecyrd.jspwiki.event.WikiEngineEvent
