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

/**
 * Closes a WikiForm.
 * 
 * @author ebu
 */
@SuppressWarnings("unchecked")
public class FormClose
        extends FormElement {
	/**
	 * Builds a Form close tag. Removes any information on the form from the
	 * WikiContext.
	 * 
	 * {@inheritDoc}
	 */
	public String execute(WikiContext ctx, Map params)
	        throws PluginException {
		StringBuffer tags = new StringBuffer();
		tags.append("</form>\n");
		tags.append("</div>");

		// Don't render if no error and error-only-rendering is on.
		FormInfo info = getFormInfo(ctx);
		if(info != null) {
			if(info.hide()) {
				ResourceBundle rb = ctx.getBundle(WikiPlugin.CORE_PLUGINS_RESOURCEBUNDLE);
				return "<p>" + rb.getString("formclose.noneedtoshow") + "</p>";
			}
		}

		// Get rid of remaining form data, so it doesn't mess up other forms.
		// After this, it is safe to add other Forms.
		storeFormInfo(ctx, null);

		return tags.toString();

	}
}
