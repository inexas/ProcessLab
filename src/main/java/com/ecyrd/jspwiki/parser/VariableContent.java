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
package com.ecyrd.jspwiki.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Text;
import com.ecyrd.jspwiki.NoSuchVariableException;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.render.RenderingManager;

/**
 * Stores the contents of a WikiVariable in a WikiDocument DOM tree.
 * <p>
 * When the WikiDocument is rendered, if the
 * {@link RenderingManager#WYSIWYG_EDITOR_MODE} is set to {@link Boolean#TRUE},
 * the variable declaration is rendered instead of the variable value.
 * 
 * @since 2.4
 */
public class VariableContent extends Text {
	private static final long serialVersionUID = 1L;

	private String m_varName;

	/**
	 * Create a VariableContent for the given variable.
	 * 
	 * @param varName
	 *            The name of the variable.
	 */
	public VariableContent(String varName) {
		m_varName = varName;
	}

	/**
	 * Evaluates the variable and returns the contents.
	 * 
	 * @return The rendered value of the variable.
	 */
	@Override
    public String getValue() {
		String result = "";
		WikiDocument root = (WikiDocument)getDocument();

		if(root == null) {
			// See similar note in PluginContent
			return m_varName;
		}

		WikiContext context = root.getContext();

		if(context == null)
			return "No WikiContext available: INTERNAL ERROR";

		Boolean wysiwygEditorMode = (Boolean)context.getVariable(RenderingManager.WYSIWYG_EDITOR_MODE);

		if(wysiwygEditorMode != null && wysiwygEditorMode.booleanValue()) {
			result = "[" + m_varName + "]";
		} else {
			try {
				result = context.getEngine().getVariableManager().parseAndGetValue(context, m_varName);
			} catch(NoSuchVariableException e) {
				result = JSPWikiMarkupParser.makeError("No such variable: " + e.getMessage()).getText();
			}
		}

		return StringEscapeUtils.escapeXml(result);
	}

	/**
	 * Returns exactly getValue().
	 * 
	 * @return Whatever getValue() returns.
	 */
	@Override
    public String getText() {
		return getValue();
	}

	/**
	 * Returns a debug-suitable string.
	 * 
	 * @return Debug string
	 */
	@Override
    public String toString() {
		return "VariableElement[\"" + m_varName + "\"]";
	}
}
