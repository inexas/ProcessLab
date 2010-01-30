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
package com.ecyrd.jspwiki.modules;

import java.util.Collection;
import com.ecyrd.jspwiki.WikiEngine;

/**
 * Superclass for all JSPWiki managers for modules (plugins, etc).
 */
@SuppressWarnings("unchecked")
public abstract class ModuleManager {

	/**
	 * Location of the property-files of plugins. (Each plugin should include
	 * this property-file in its jar-file)
	 */
	public static final String PLUGIN_RESOURCE_LOCATION = "ini/jspwiki_module.xml";

	protected WikiEngine m_engine;

	@SuppressWarnings("unused")
    private boolean m_loadIncompatibleModules = false;

	/**
	 * Constructs the ModuleManager.
	 * 
	 * @param engine
	 *            The WikiEngine which owns this manager.
	 */
	public ModuleManager(WikiEngine engine) {
		m_engine = engine;
	}

	/**
	 * Returns true, if the given module is compatible with this version of
	 * JSPWiki.
	 * 
	 * @param info
	 *            The module to check
	 * @return True, if the module is compatible.
	 */
	public boolean checkCompatibility(@SuppressWarnings("unused") WikiModuleInfo info) {
		// KW wikibi modification
//		if(!m_loadIncompatibleModules) {
//			String minVersion = info.getMinVersion();
//			String maxVersion = info.getMaxVersion();
//
//			return Release.isNewerOrEqual(minVersion) && Release.isOlderOrEqual(maxVersion);
//		}

		return true;
	}

	/**
	 * Returns a collection of modules currently managed by this ModuleManager.
	 * Each entry is an instance of the WikiModuleInfo class. This method should
	 * return something which is safe to iterate over, even if the underlying
	 * collection changes.
	 * 
	 * @return A Collection of WikiModuleInfo instances.
	 */
	public abstract Collection modules();
}
