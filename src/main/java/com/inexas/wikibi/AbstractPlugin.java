package com.inexas.wikibi;

import java.util.*;
import javax.servlet.http.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.plugin.*;
import com.inexas.wikibi.datasource.*;
import com.inexas.wikibi.scheduler.*;

/**
 * This is a simple JSPWiki plugin that generates a result set based on a SQL
 * query.
 * 
 * @author Keith Whittingham, Processwide
 */
abstract class AbstractPlugin implements WikiPlugin, HasJobs {
	private static final String SUBMIT = "dataSubmit";
	private final List<String> dbAccessErrors = new ArrayList<String>();
	private final List<String> fieldErrors = new ArrayList<String>();
	private final List<String> userErrors = new ArrayList<String>();
	protected final StringBuilder sb = new StringBuilder();
	protected boolean submitted;
	protected WikiContext context;
	// Output buffer
	protected HttpServletRequest request;
	protected boolean isRefresh;
	protected Parameters parameters;
	
	/**
	 * Perform initialization tasks common to all subclasses. We return false if
	 * an error is made in the declaration of the plugin that would force
	 * re-editing of the page; for example a missing mandatory parameter. If
	 * such an error is spotted then an error message is written to the sb so
	 * the plugin should cease processing.
	 * 
	 * @param context
	 * @param pluginParameters
	 * @param sb
	 * @param sqlType
	 * @return false if there's an error in the declaration of the plugin
	 */
	@SuppressWarnings("unchecked")
	public String execute(WikiContext theContext, Map pluginParameters) throws PluginException {
		context = theContext;
		
		parameters = new Parameters(this, pluginParameters);
		
		boolean gotAllFields = true;
		
		if(parameters.areErrorFree()) {
			// We're running in a servlet...
			final HttpServletRequest httpRequest = context.getHttpRequest();
			request = httpRequest;
			isRefresh = "t".equals(httpRequest.getParameter("rf"));
			
			final String db = parameters.getDb();
			if(db != null) {
				// We have a SQL statement, if there are any fields set the
				// submitted values if there was a submit otherwise set the
				// default values if any....
				
				SourcePool.Factory factory = SourcePool.getInstance().getFactory(request, db);
				
				submitted = getFieldValue(SUBMIT) != null;
				final Map<String, String> map = new HashMap<String, String>();
				if(submitted) {
					// The submit button was pressed, try and fill in the fields
					boolean seenNullValue = false;
					final String[] fields = parameters.getFields();
					if(fields != null) {
						for(final String key : parameters.getFields()) {
							final String value = getFieldValue(key);
							if(value == null) {
								seenNullValue = true;
								break;
							}
							map.put(key, value);
						}
					}
					
					if(seenNullValue) {
						userErrors.add("All fields are mandatory");
					} else {
						parameters.resolveFields(map);
						validateSql(factory);
					}
				} else {
					// Submit key not pressed. Write any default values that come
					// in as plugin parameters into the SQL statement and see if we
					// have a complete query...
					// !todo find out how this is supposed to work and document it!
					final String[] fields = parameters.getFields();
					if(fields != null) {
						for(final String key : parameters.getFields()) {
							final String value = getFieldValue(key);
							if(value == null) {
								gotAllFields = false;
								break;
							}
							map.put(key, value);
						}
						
						if(gotAllFields) {
							validateSql(factory);
						}
					}
				}
			}
		}
		

		// Preparation over, let's render...

		final boolean error;
		if(!parameters.areErrorFree()) {
			sb.append("<p><b><font color=red>Error: </font></b>");
			sb.append(parameters.getErrors().get(0));
			sb.append("<br/>You need to edit the page to fix the problem.");
			sb.append("</p>");
			error = true;
		} else if(!dbAccessErrors.isEmpty()) {
			sb.append("<p><b><font color=red>Error: </font></b>");
			sb.append(dbAccessErrors.get(0));
			sb.append("<br/>You need to edit the page to fix the problem.");
			sb.append("</p>");
			error = true;
		} else if(!userErrors.isEmpty()) {
			sb.append("<p><b><font color=red>Error: </font></b>");
			sb.append(fieldErrors.get(0));
			sb.append("<br/>Correct the fields and try again");
			sb.append("</p>");
			error = true;
		} else {
			error = false;
		}

		buildFieldForm();

		if(gotAllFields && !error) {
			try {
				render();
			} catch(final Exception e) {
				sb.setLength(0);
				sb.append("<div class=error>");
				sb.append(e.getMessage());
				sb.append("</div>");
				// todo Log the stack trace
			}
		}

		return sb.toString();
	}

	abstract void render();

	/**
	 * Validate the SQL. Check the SQL starts with a valid leader: SELECT,
	 * UPDATE, etc.. Also check that there's only one statement to avoid SQL
	 * injection in fields.
	 */
	private void validateSql(SourcePool.Factory factory) {

		// Check the leaders
		final String[] leaders;
		if(this instanceof DataView) {
			leaders = factory.getViewLeaders();
		} else {
			leaders = factory.getUpdateLeaders();
		}

		final String normalSql = parameters.getSql().trim().toUpperCase();
		boolean found = false;
		for(final String leader : leaders) {
			if(normalSql.startsWith(leader)) {
				found = true;
				break;
			}
		}
		if(!found) {
			// Construct an appropriate error message...
			final StringBuilder errorMessage = new StringBuilder();
			String delimiter = "Only ";
			for(final String leader1 : leaders) {
				errorMessage.append(delimiter);
				delimiter = ", ";
				errorMessage.append(leader1);
			}
			errorMessage.append(" statements allowed");
			dbAccessErrors.add(errorMessage.toString());
		}
	}

	private void buildFieldForm() {
		final String[] fields = parameters.getFields();
		if((fields != null) || this instanceof DataUpdate) {
			// Start the form...
			sb.append("<form " +
			        "name=dataForm " +
			        "accept-charset=UTF-8 " +
			        "enctype=\"application/x-www-form-urlencoded\" " +
			        "method=post " +
			        "action=");
			sb.append(getUrl());
			sb.append("><table>");

			// Build a form for the fields...
			if(fields != null) {
				for(final String fieldName : fields) {
					final String fieldValue = getFieldValue(fieldName);
					sb.append("<tr><td>");
					sb.append(makeLabel(fieldName));
					sb.append("</td><td><input type=text name=");
					sb.append(fieldName);
					if(fieldValue != null) {
						sb.append(" value=\"");
						sb.append(fieldValue);
						sb.append('"');
					}
					sb.append(" /></td></tr>");
				}
			}

			// Add the submit button...
			sb.append(
			        "<tr>" +
			        "<td>&nbsp;</td>" +
			        "<td><input name=" + SUBMIT + " type=submit value=\"Submit\"/></td>" +
			        "</tr>");
			sb.append("</table></form><br>");
		}

	}

	private String getUrl() {
		final String result;
		if(context == null) {
			result = "http://www.inexas.com/wikibi/TestPageName/";
		} else {
			final String pageName = context.getPage().getName();
			result = context.getURL(WikiContext.VIEW, pageName);
		}
		return result;
	}

	/**
	 * We are given a string with underlines_in_it, convert single underlines to
	 * space and double underlines to a single underline
	 * 
	 * @param name
	 * @return
	 */
	public static String makeLabel(String name) {
		final char[] input = name.toCharArray();
		final int length = input.length;
		final char[] output = new char[length];
		int i = 0, o = 0;
		while(i < length) {
			char c = input[i];
			i++;
			if(i < length) {
				if(c == '_') {
					if(input[i] == '_') {
						i++;
					} else {
						c = ' ';
					}
				}
			}
			output[o++] = c;
		}
		return new String(output, 0, o);
	}

	private String getFieldValue(String fieldName) {
		final String result;
		if(context == null) {
			result = null;
		} else {
			final String value = context.getHttpParameter(fieldName);
			if(value == null) {
				// Try for a default value..
				// !todo Fix me result = Parameter.getStringParameter(fieldName + ".default");
				result = null;
			} else {
				final String trimmed = value.trim();
				result = trimmed.length() == 0 ? null : trimmed;
			}
		}
		return result;
	}

	public Job getJobs(WikiContext pageContext, Map<String, String> pluginParameters) { 
		this.context = pageContext;

		parameters = new Parameters(this, pluginParameters);

		final Job result;
		final String cronSchedule = parameters.getCronSchedule();
		if(parameters.areErrorFree() && cronSchedule != null) {
			final Schedulable schedulable = new DataSchedulable(this);
			result = new Job(schedulable, cronSchedule);
		} else {
			result = null;
		}
		return result;
    }
	
	public Parameters getParameters() {
		return parameters;
	}
}
