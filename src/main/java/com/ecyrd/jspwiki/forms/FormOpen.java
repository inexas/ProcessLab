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

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

/**
 * Opens a WikiForm.
 * 
 * Builds the HTML code for opening a FORM.
 * 
 * <p>
 * Since we're only providing an opening FORM tag, we can't use the ECS
 * utilities.
 * 
 * A Form plugin line that produces one looks like this:
 * <p>
 * 
 * <pre>
 *   [{FormOpen name='formname' handler='pluginname'
 *          submit='submitservlet'
 *          show='always'
 *   }]
 * </pre>
 * 
 * <p>
 * Mandatory parameters: <br>
 * The <i>name</i> field identifies this particular form to the Form plugin
 * across pages. <br>
 * The <i>handler</i> field is a WikiPlugin name; it will be invoked with the
 * form field values.
 * 
 * <p>
 * Optional parameters:
 * <p>
 * The submitservlet is the name of a JSP/servlet capable of handling the input
 * from this form. It is optional; the default value is the current page (which
 * can handle the input by using this Plugin.)
 * 
 * <p>
 * The <i>hide</i> parameter affects the visibility of this form. If left out,
 * the form is always shown. If set to 'onsuccess', the form is not shown if it
 * was submitted successfully. (Note that a reload of the page would cause the
 * context to reset, and the form would be shown again. This may be a useless
 * option.)
 * 
 * @author ebu
 */
@SuppressWarnings("unchecked")
public class FormOpen
        extends FormElement {
	private static org.apache.log4j.Logger log =
	        org.apache.log4j.Logger.getLogger(FormOpen.class);

	/**
	 * Parameter name for setting the method (GET or POST). Value is
	 * <tt>{@value}</tt>.
	 */
	public static final String PARAM_METHOD = "method";

	/**
	 * {@inheritDoc}
	 */
	public String execute(WikiContext ctx, Map params)
	        throws PluginException {
		ResourceBundle rb = ctx.getBundle(WikiPlugin.CORE_PLUGINS_RESOURCEBUNDLE);
		String formName = (String)params.get(PARAM_FORM);
		if(formName == null) {
			Object[] args = { PARAM_FORM };
			throw new PluginException(MessageFormat.format(rb.getString("formopen.missingparam"), args));
		}
		String hide = (String)params.get(PARAM_HIDEFORM);
		String sourcePage = ctx.getPage().getName();
		String submitServlet = (String)params.get(PARAM_SUBMITHANDLER);
		if(submitServlet == null)
			submitServlet = ctx.getURL(WikiContext.VIEW, sourcePage);

		String method = (String)params.get(PARAM_METHOD);
		if(method == null)
			method = "post";

		if(!(method.equalsIgnoreCase("get") || method.equalsIgnoreCase("post"))) {
			throw new PluginException(rb.getString("formopen.postorgetonly"));
		}

		FormInfo info = getFormInfo(ctx);
		if(info != null) {
			// Previous information may be the result of submitting
			// this form, or of a FormSet plugin, or both. If it
			// exists and is for this form, fine.
			if(formName.equals(info.getName())) {
				log.debug("Previous FormInfo for this form was found in context.");
				// If the FormInfo exists, and if we're supposed to display on
				// error only, we need to exit now.
				if(hide != null &&
				        HIDE_SUCCESS.equals(hide) &&
				        info.getStatus() == FormInfo.EXECUTED) {
					info.setHide(true);
					return "<p>" + rb.getString("formopen.noneedtoshow") + "</p>";
				}
			} else {
				// This would mean that a new form was started without
				// closing an old one. Get rid of the garbage.
				info = new FormInfo();
			}
		} else {
			// No previous FormInfo available; store now, so it'll be
			// available for upcoming Form input elements.
			info = new FormInfo();
			storeFormInfo(ctx, info);
		}

		info.setName(formName);
		info.setAction(submitServlet);

		StringBuffer tag = new StringBuffer(40);
		tag.append("<div class=\"wikiform\">\n");
		tag.append("<form action=\"" + submitServlet);
		tag.append("\" name=\"" + formName);
		tag.append("\" accept-charset=\"" + ctx.getEngine().getContentEncoding());
		tag.append("\" method=\"" + method + "\" enctype=\"application/x-www-form-urlencoded\">\n");
		tag.append("  <input type=\"hidden\" name=\"" + PARAM_FORMNAMEHIDDEN);
		tag.append("\" value=\"" + formName + "\"/>\n");

		return tag.toString();
	}

}
