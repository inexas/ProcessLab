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
package com.ecyrd.jspwiki.forms;

import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;
import java.util.*;
import org.apache.ecs.xhtml.input;

/**
 * Creates a simple input text field.
 */
@SuppressWarnings("unchecked")
public class FormInput
        extends FormElement {
	/** Parameter name for setting the type. Value is <tt>{@value}</tt>. */
	public static final String PARAM_TYPE = "type";

	/**
	 * Parameter name for setting the size of the input field. Value is
	 * <tt>{@value}</tt>.
	 */
	public static final String PARAM_SIZE = "size";

	/**
	 * Generates a dynamic form element on the WikiPage.
	 * 
	 * {@inheritDoc}
	 */
	public String execute(WikiContext ctx, Map params)
	        throws PluginException {
		String inputName = (String)params.get(PARAM_INPUTNAME);
		String inputValue = (String)params.get(PARAM_VALUE);
		String inputType = (String)params.get(PARAM_TYPE);
		String size = (String)params.get(PARAM_SIZE);
		ResourceBundle rb = ctx.getBundle(WikiPlugin.CORE_PLUGINS_RESOURCEBUNDLE);

		if(inputName == null)
			throw new PluginException(rb.getString("forminput.namemissing"));
		if(inputValue == null)
			inputValue = "";

		// Don't render if no error and error-only-rendering is on.
		FormInfo info = getFormInfo(ctx);
		Map previousValues = null;
		if(info != null) {
			if(info.hide()) {
				return "<p>" + rb.getString("forminput.noneedtoshow") + "</p>";
			}
			previousValues = info.getSubmission();
		}

		if(previousValues == null) {
			previousValues = new HashMap();
		}

		// In order to isolate posted form elements into their own
		// map, prefix the variable name here. It will be stripped
		// when the handler plugin is executed.
		input field = new input(inputType,
		        HANDLERPARAM_PREFIX + inputName,
		        inputValue);

		String checked = (String)params.get("checked");
		field.setChecked(TextUtil.isPositive(checked)
		        || "checked".equalsIgnoreCase(checked));

		String oldValue = (String)previousValues.get(inputName);
		if(oldValue != null) {
			field.setValue(oldValue);
		}

		if(size != null)
			field.setSize(size);

		return field.toString(ctx.getEngine().getContentEncoding());
	}
}
