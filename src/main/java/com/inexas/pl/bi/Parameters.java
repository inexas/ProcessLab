/**
 * Copyright (C) Inexas 2009, All rights reserved.
 *
 * @author keith
 */
package com.inexas.pl.bi;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

/**
 * The Parameter class hierarchy is to standardize the parameters used for all
 * DataStar plugins. The Google visualizations are nearly, but not quite,
 * consistent and there are lots of them.</p>
 * 
 * Some parameters are common to all formats like SIZE. Others, like SMOOTH are
 * not. Some parameters must always be rendered like dataMode for markers or
 * regions maps. Formats have default values for many of the parameters. We will
 * avoid the overhead of explicitly putting the defaults in if we can.</p>
 * 
 * We encapsulate:</p>
 * 
 * <ui>
 * 
 * <li>Which parameters should be there and which not for each plugin
 * 
 * <li>How to read the parameter, what the default value is, what is valid
 * 
 * <li>How to write the value
 * 
 * </ui>
 */
class Parameters {
	static abstract class Parameter<T> {
		protected final String key;
		protected String googleName;
		protected T value;
		protected boolean alwaysAdd;
		protected boolean neverAdd;

		protected Parameter(String key) {
			googleName = this.key = key;
		}

		/**
		 * Copy constructor used when the value is retrieved from the plugin
		 * parameters
		 */
		protected Parameter(Parameter<?> toCopy, T value) {
			key = toCopy.key;
			googleName = toCopy.googleName;
			alwaysAdd = toCopy.alwaysAdd;
			neverAdd = toCopy.neverAdd;
			this.value = value;
			
		}

		/**
		 * Parse the value of a plugin.
		 */
		abstract Parameter<?> validate(
		        AbstractPlugin plugin,
		        Map<String, String> pluginParameters,
		        List<String> errors);

		abstract Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors);
		
		abstract void toJavaScript(StringBuilder sb);
		
		public T getValue() {
			return value;
		}
	}

	private static abstract class NonStringParameter<T> extends Parameter<T> {
		protected NonStringParameter(String name) {
			super(name);
		}

		protected NonStringParameter(Parameter<?> toCopy, T value) {
			super(toCopy, value);
		}

		/**
		 * Non-string parameters render without apostrophes
		 */
		void toJavaScript(StringBuilder sb) {
			sb.append(googleName);
			sb.append(':');
			sb.append(value.toString());
		}
	}

	static class StringParameter extends Parameter<String> {
		protected StringParameter(String name) {
			super(name);
		}

		protected StringParameter(Parameter<?> toCopy, String value) {
			super(toCopy, value);
		}

		void toJavaScript(StringBuilder sb) {
			sb.append(googleName);
			sb.append(":'");
			// todo Think about escaping any apostrophes
			sb.append(value);
			sb.append('\'');
		}

		@Override
		public Parameter<String> validate(
		        AbstractPlugin plugin,
		        Map<String, String> pluginParameters,
		        List<String> errors) {
			final String string = getStringParameter(pluginParameters, key);
			return string == null ? null : new StringParameter(this, string);
		}

		@Override
        Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors) {
			return string == null ? null : new StringParameter(this, string);
        }
	}

	private static class BooleanParameter extends NonStringParameter<Boolean> {
		BooleanParameter(String name) {
			super(name);
			value = Boolean.FALSE;
		}

		BooleanParameter(BooleanParameter toCopy, Boolean value) {
			super(toCopy, value);
		}

		@Override
		BooleanParameter validate(
				AbstractPlugin plugin,
				Map<String, String> pluginParameters,
				List<String> errors) {
			final String string = getStringParameter(pluginParameters, key);
			return (BooleanParameter)validate(plugin, string, errors);
		}

		@Override
        Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors) {
			final boolean b;
			if(string == null || string.equals(FALSE)) {
				b = false;
			} else if(string.equals(TRUE)) {
				b = true;
			} else {
				errors.add("Value of parameter " + key + " must be '" + TRUE + "' or '" + FALSE + "'");
				b = false;
			}
			return b ? new BooleanParameter(this, Boolean.TRUE) : null;
        }
	}

	private static class IntegerParameter extends NonStringParameter<Integer> {
		final int minimumValue, maximumValue;
		int intValue;

		IntegerParameter(String name, int minimumValue, int maximumValue) {
			super(name);
			this.minimumValue = minimumValue;
			this.maximumValue = maximumValue;
			intValue = -1; // This should never be used
		}

		IntegerParameter(String name, int minimumValue, int maximumValue, int initialValue) {
			super(name);
			this.minimumValue = minimumValue;
			this.maximumValue = maximumValue;
			intValue = initialValue;
			value = new Integer(initialValue);
		}

		IntegerParameter(IntegerParameter toCopy, int value) {
			super(toCopy, new Integer(value));
			minimumValue = toCopy.minimumValue;
			maximumValue = toCopy.maximumValue;
			intValue = toCopy.intValue;
		}

		IntegerParameter(SizeParameter toCopy, int value) {
			super(toCopy, new Integer(value));
			minimumValue = -1;
			maximumValue = -1;
			intValue = value;
		}

		@Override
		IntegerParameter validate(
		        AbstractPlugin plugin,
		        Map<String, String> pluginParameters,
		        List<String> errors) {
			final String string = getStringParameter(pluginParameters, key);
			return (IntegerParameter)validate(plugin, string, errors);
		}

		@Override
        Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors) {
			IntegerParameter result;
			if(string == null) {
				result = null;
			} else {
				try {
					final int i = Integer.parseInt(string);
					if(i >= minimumValue && i <= maximumValue) {
						result = new IntegerParameter(this, i);
					} else {
						errors.add("Invalid value for parameter " + key + ": " + string);
						result = null;
					}
				} catch(final NumberFormatException e) {
					errors.add("Invalid value for parameter " + key + ": " + string);
					result = null;
				}
			}
			return result;
        }

		public int intValue() {
			return value.intValue();
		}
	}

	private static class SqlParameter extends StringParameter {
		SqlParameter() {
			super(SQL_KEY);
			neverAdd = true;
		}

		private SqlParameter(SqlParameter toCopy, String value) {
			super(toCopy, value);
		}

		@Override
		public SqlParameter validate(
		        AbstractPlugin plugin,
		        Map<String, String> pluginParameters,
		        List<String> errors) {
			final SqlParameter result;
			final String string = getStringParameter(pluginParameters, key);
			if(string == null) {
				result = this;
			} else {
				// Check for multiple statements...
				if(string.indexOf(';') >= 0) {
					// todo Find a more intelligent way of blocking multiple
					// statements
					errors.add("Semi-colons not permitted in SQL statements");
					result = null;
				} else {
					result = new SqlParameter(this, string);
				}
			}
			return result;
		}
	}

	private static class LegendParameter extends StringParameter {
		LegendParameter() {
			super(LEGEND_KEY);
		}

		private LegendParameter(LegendParameter toCopy, String value) {
			super(toCopy, value);
		}

		@Override
		public Parameter<String> validate(AbstractPlugin plugin, Map<String, String> pluginParameters,
		        List<String> errors) {
			final LegendParameter result;
			final String string = getStringParameter(pluginParameters, key);
			if(string == null) {
				result = null;
			} else {
				if(string.equals("top") || string.equals("bottom") || string.equals("left") ||
						string.equals("right") || string.equals("none")) {
					result = new LegendParameter(this, string);
				} else {
					errors.add("Invalid value for parameter: " + key +
					", it can be one of the following: 'top', 'bottom', 'left', 'right' or 'none'");
					result = null;
				}
			}
			return result;
		}
	}

	static class SizeParameter extends Parameter<String> {
		private int height, width;
		private int[] limits;
		private Format format;

		SizeParameter() {
			super(SIZE_KEY);
			this.height = 1;	// Corresponds to small
			this.width = -1;
//			this.alwaysAdd = true;
		}

		private SizeParameter(SizeParameter toCopy, int height, int width) {
			super(toCopy, null);
			this.height = height;
			this.width = width;
		}

		private SizeParameter(SizeParameter toCopy, String size) {
			super(toCopy, size);
			this.height = -1;
			this.width = -1;
		}

		public void setLimits(int[] limits) {
			this.limits = limits;
		}

		@Override
		public Parameter<?> validate(
		        AbstractPlugin plugin,
		        Map<String, String> pluginParameters,
		        List<String> errors) {
			final String string = getStringParameter(pluginParameters, key);
			return validate(plugin, string, errors);
		}


		@Override
		Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors) {
			Parameter<?> result;
			if(string == null) {
				result = null;
			} else {
				if(format == Format.org) {
					switch(Size.valueOf(string)) {
					case xs:
					case s:
						result = new StringParameter(this, "small");
						break;
						
					case m:
						result = new StringParameter(this, "medium");
						break;
						
					case l:
					case xl:
						result = new StringParameter(this, "large");
						break;
						
					default:
						errors.add("Invalid value for parameter size: " + value);
						result = null;
					}
				} else if(format == Format.table) {
					final IntegerParameter integerParameter;
					switch(Size.valueOf(string)) {
					case xs:
						integerParameter = new IntegerParameter(this, 5);
						break;
					case s:
						integerParameter = new IntegerParameter(this, 20);
						break;
					case m:
						integerParameter = new IntegerParameter(this, 50);
						break;
					case l:
						integerParameter = new IntegerParameter(this, 100);
						break;
					case xl:
						integerParameter = new IntegerParameter(this, 1000);
						break;
					default:
						errors.add("Invalid value for parameter size: " + value);
						integerParameter = null;
						break;
					}
					if(integerParameter != null) {
						integerParameter.googleName = "page:'enable',pageSize";
					}
					result = integerParameter;
				} else {
					try {
						final int theHeight, theWidth;
						final int comma = string.indexOf(',');
						if(comma > 0) {
							// It's size='200,300'
							theHeight = Integer.parseInt(string.substring(0, comma));
							theWidth = Integer.parseInt(string.substring(comma + 1));
						} else {
							switch(Size.valueOf(string)) {
							case xs:
								theHeight = 0;
								break;
							case s:
								theHeight = 1;
								break;
							case m:
								theHeight = 2;
								break;
							case l:
								theHeight = 3;
								break;
							case xl:
								theHeight = 4;
								break;
							default:
								throw new RuntimeException("Invalid size: " + string);
							}
							theWidth = -1;
						}
						result = new SizeParameter(this, theHeight, theWidth);
					} catch(final NumberFormatException e) {
						errors.add("Invalid value for parameter size: " + value);
						result = null;
					}
				}
			}
			return result;
        }

		@Override
		void toJavaScript(StringBuilder sb) {
			// height:200,width:300
			calulateSize();
			sb.append("height:");
			sb.append(height);
			sb.append(",width:");
			sb.append(width);
		}

		void toStyle(StringBuilder sb) {
			// height:2500px;width:750px;
			calulateSize();
			sb.append("height:");
			sb.append(height);
			sb.append("px;width:");
			sb.append(width);
			sb.append("px;");
		}
		
		private void calulateSize() {
			if(width == -1) {
				final int index = height;
				final int smallestHeight = limits[0];
				final int largestHeight = limits[1];
				final int smallestWidth = limits[2];
				final int largestWidth = limits[3];
				height = smallestHeight + index * (largestHeight - smallestHeight) / 4;
				width = smallestWidth + index * (largestWidth - smallestWidth) / 4;
			}
		}
	}

	private static class FormatParameter extends Parameter<Format> {
		FormatParameter() {
			super(FORMAT_KEY);
			alwaysAdd = true;
			neverAdd = true;
		}

		private FormatParameter(FormatParameter toCopy, Format value) {
			super(toCopy, value);
		}

		@Override
		public Parameter<Format> validate(
				AbstractPlugin plugin,
				Map<String, String> pluginParameters,
		        List<String> errors) {
			final String string = getStringParameter(pluginParameters, key);
			return (FormatParameter)validate(plugin, string, errors);
		}

		@Override
        Parameter<?> validate(AbstractPlugin plugin, String string, List<String> errors) {
			Format format;
			if(string == null) {
				format = Format.table;
			} else {
				try {
					format = Format.valueOf(string);
				} catch(final Exception e) {
					errors.add("Invalid format value: " + string);
					format = null;
				}
			}
			return format == null ? null : new FormatParameter(this, format);
        }

		@Override
		void toJavaScript(StringBuilder sb) {
			// Nothing to render
		}
	}

	private static enum Size {
		xs,
		s,
		m,
		l,
		xl,
	}

	// !todo Document spreadsheet, schedule
	// !todo CSV with not enough headers, error message disappears
	enum Format {
		area(AreaChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				STACK, MIN, MAX, LEGEND),
		bar(HorizontalBarChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				STACK, MIN, MAX, LEGEND, IS3D),
		column(VerticalBarChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				STACK, MIN, MAX, LEGEND, IS3D),
		gauge(GaugeChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				MIN, MAX),
		intensity(IntensityMapRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				REGION),
		line(LineChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				MIN, MAX, LEGEND, SMOOTH),
		marker(MarkerMapRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				MARKERS, REGION),
		motion(MotionChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				STATE),
		text(PlainTextRenderer.class, SPREADSHEET, CACHED, SIZE, LINK),
		org(OrgChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				COLLAPSE),
				// addNonStringParameter("legendFontSize", "10");
		pie(PieChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				IS3D, LEGEND, LEGENDFONT),
		region(RegionMapRenderer.class, REGIONS, SPREADSHEET, CACHED, SIZE, LINK,
				REGION),
		scatter(ScatterChartRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				MIN, MAX, LEGEND),
		spreadsheet(SpreadsheetRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				TARGET),
		timeline(TimeLineChartRenderer.class, ANNOTATE, SPREADSHEET, CACHED, SIZE, LINK),
		table(TableRenderer.class, SPREADSHEET, CACHED, SIZE, LINK,
				ROWNUMBERS, PAGING, MAX);

		private final Map<String, Parameters.Parameter<?>> parameters =
		        new HashMap<String, Parameters.Parameter<?>>();

		final Constructor<? extends Renderer> rendererConstructor;

		private Format(Class<? extends Renderer> rendererClass, Parameters.Parameter<?>... validatorArray) {
			try {
				final Class<?>[] rendererParameters = { DataView.class };
				this.rendererConstructor = rendererClass.getConstructor(rendererParameters);
			} catch(final Exception e) {
				throw new RuntimeException("Error loading ctor", e);
			}
			for(final Parameters.Parameter<?> parameter : validatorArray) {
				parameters.put(parameter.key, parameter);
			}
		}
	}

	private static final String TRUE = Boolean.TRUE.toString();
	private static final String FALSE = Boolean.FALSE.toString();
	private static final String DB_KEY = "db";
	private static final String SQL_KEY = "sql";
	private static final String BODY_KEY = "_body";
	private static final String SCHEDULE_KEY = "schedule";
	private static final String LINK_KEY = "link";
	private static final String FORMAT_KEY = "format";
	private static final String SIZE_KEY = "size";
	private static final String CACHED_KEY = "cached";
	private static final String SPREADSHEET_KEY = "spreadsheet";
	private static final String DATAMODE_KEY = "dataMode";
	private static final String COLLAPSE_KEY = "collapse";
	private static final String ROWNUMBERS_KEY = "rownumbers";
	private static final String PAGE_KEY = "page";
	private static final String STACK_KEY = "stack";
	private static final String SMOOTH_KEY = "smooth";
	private static final String ANNOTATE_KEY = "annotate";
	private static final String IS3D_KEY = "is3d";
	private static final String LEGEND_KEY = "legend";
	private static final String MIN_KEY = "min";
	private static final String MAX_KEY = "max";
	public static final String TARGET_KEY = "target";
	private static final String REGION_KEY = "region";
	private static final String STATE_KEY = "state";
	private static final String LEGENDFONT_KEY = "legendFontSize";
	// Parameters that are common to all plugins...
	private static final StringParameter DB = new StringParameter(DB_KEY);
	private static final StringParameter SQL = new SqlParameter();
	private static final StringParameter BODY = new StringParameter(BODY_KEY);
	private static final StringParameter SCHEDULE = new StringParameter(SCHEDULE_KEY);
	// Parameters that are common to all view formats...
	private static final FormatParameter FORMAT = new FormatParameter();
	private static final IntegerParameter CACHED = new IntegerParameter(CACHED_KEY, 1, 28 * 24 * 20);
	private static final BooleanParameter SPREADSHEET = new BooleanParameter(SPREADSHEET_KEY);
	private static final SizeParameter SIZE = new SizeParameter();
	private static final StringParameter LINK = new StringParameter(LINK_KEY);
	// Always render parameters...
	private static final StringParameter MARKERS = new StringParameter(DATAMODE_KEY);
	private static final BooleanParameter COLLAPSE = new BooleanParameter(COLLAPSE_KEY);
	private static final StringParameter REGIONS = new StringParameter(DATAMODE_KEY);
	private static final BooleanParameter ROWNUMBERS = new BooleanParameter(ROWNUMBERS_KEY);
	private static final StringParameter PAGING = new StringParameter(PAGE_KEY);
	// Optional parameters
	private static final BooleanParameter STACK = new BooleanParameter(STACK_KEY);
	private static final BooleanParameter SMOOTH = new BooleanParameter(SMOOTH_KEY);
	private static final BooleanParameter ANNOTATE = new BooleanParameter(ANNOTATE_KEY);
	private static final BooleanParameter IS3D = new BooleanParameter(IS3D_KEY);
	private static final LegendParameter LEGEND = new LegendParameter();
	private static final IntegerParameter MIN = new IntegerParameter(
			MIN_KEY, Integer.MAX_VALUE, Integer.MAX_VALUE);
	private static final IntegerParameter MAX = new IntegerParameter(
			MAX_KEY, Integer.MAX_VALUE, Integer.MAX_VALUE);
	private static final StringParameter TARGET = new StringParameter(TARGET_KEY);
	private static final StringParameter REGION = new StringParameter(REGION_KEY);
	private static final StringParameter STATE = new StringParameter(STATE_KEY);
	private static final IntegerParameter LEGENDFONT = new IntegerParameter(LEGENDFONT_KEY, 6, 60, 10);
	// Incorporate any remaining idiomatic behavior...
	static {
		BODY.neverAdd = true;
		MARKERS.value = "markers";
		MARKERS.alwaysAdd = true;
		DB.neverAdd = true;
		SQL.neverAdd = true;
		LEGENDFONT.alwaysAdd = true;
		REGIONS.value = "regions";
		COLLAPSE.googleName = "allowCollapse";
		ROWNUMBERS.googleName = "showRowNumber";
		PAGING.value = "enable";
		STACK.googleName = "isStacked";
		SMOOTH.googleName = "smoothLine";
		ANNOTATE.googleName = "displayAnnotations";
		IS3D.googleName = "is3D";
		// todo! Migrate this and other default settings to classes if I can
		FORMAT.value = Format.table;
	}
	private static final Pattern identifierPattern = Pattern.compile("@[a-zA-Z][a-zA-Z0-9_]*@");
	private final Map<String, Parameter<?>> parameters = new HashMap<String, Parameter<?>>();
	private List<String> errors = new ArrayList<String>();
	private final AbstractPlugin plugin;
	private final Map<String, String> inputParameters;
	private Csv csv;
	// !todo Think about getting rid of the pre-processed params.
	private String db, sql;
	private String cronSchedule;
	private int cached;
	private String[] fields;
	private Format format;
	private int[] limits;

	Parameters(AbstractPlugin plugin, Map<String, String> pluginParameters) {
		this.plugin = plugin;

		/*
		 * Make a copy of the parameters. We remove each valid parameter as it
		 * is processed. Remove and unneeded parameters. At the end of
		 * processing we either have an empty parameter map or some warnings to
		 * emit.
		 */
		inputParameters = new HashMap<String, String>(pluginParameters);
		// Remove any JSPWiki parameters...
		inputParameters.remove("_bounds");
		inputParameters.remove("_cmdline");

		readAndValidateCommonParameters();

		if(plugin instanceof DataView && errors.isEmpty()) {
			readAndValidateDataViewParameters();
		}
	}

	private void readAndValidateCommonParameters() {
		db = process(DB);
		sql = process(SQL);
		final String body = process(BODY);
		cronSchedule = process(SCHEDULE);

		// Figure out SQL or inlined data..
		if(db == null) {
			if(sql != null) {
				errors.add("Cannot have 'sql' parameter when no DB is specified");
			}
			if(body == null) {
				errors.add("Missing inlined data");
			}
		} else { // db was not null
			if(sql == null && body == null) {
				errors.add("Either a 'sql' paramter or the plugin body should contain a SQL statement");
			} else if(sql != null && body != null) {
				errors.add("Both 'sql' parameter and a body cannot accompany a 'db' parameter");
			}
			if(sql == null) {
				sql = body;
			}

			fields = findFields(sql);
		}
		// Now either db is null and it's in line data or both db and sql
		// are not null

		if(db == null && body != null && errors.isEmpty()) {
			csv = new Csv(body);
		}

		if(cronSchedule != null) {
			if(db == null) {
				errors.add("Scheduled jobs must have a db parameter and an SQL statement");
			} else if(fields != null) {
				errors.add("Scheduled jobs cannot have @fields@");
			}
		}
	}

	private void readAndValidateDataViewParameters() {
		format = process(FORMAT);

		if(format == null) {
			errors.add("Missing parameter: 'format'");
		} else {
			for(final Entry<String, String> entry : inputParameters.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				final Parameter<?> parameter = format.parameters.get(key);
				if(parameter == null) {
					errors.add("Unexpected paramater: " + key);
				} else {
					if(parameter instanceof SizeParameter) {
						final SizeParameter sizeParameter = (SizeParameter)parameter;
						sizeParameter.format = format;
					}
					final Parameter<?> processed = parameter.validate(plugin, value, errors);
					if(processed != null) {
						parameters.put(key, processed);
					}
				}
			}
			
			// Check for any always-add parameters that we haven't added...
			for(final Parameter<?> parameter : format.parameters.values()) {
				final String key = parameter.key;
				if(parameter.alwaysAdd && !parameters.containsKey(key)) {
					parameters.put(key, parameter);
				}
			}
		}
		
		if(errors.isEmpty()) {
			cached = getIntegerValue(CACHED_KEY, 0);
			
			if(cronSchedule != null && cached == 0) {
				errors.add("Cannot schedule an uncached DataView, add 'cached' or remove 'schedule'");
			}
		}
	}

	/**
	 * Limits must be set if the parameters are to be rendered
	 * 
	 * @param limits
	 */
	void setLimits(int[] limits) {
		this.limits = limits;
	}

	/**
	 * Replace all the fields in the SQL input. We might for example get "SELECT
	 * ... WHERE id=@id@
	 * 
	 * @param map
	 * 
	 * @param input
	 * @return
	 */
	void resolveFields(Map<String, String> map) {
		for(final Entry<String, String> entry : map.entrySet()) {
			final String key = "@" + entry.getKey() + "@";
			final Object value = entry.getValue();
			if(value == null) {
				continue;
			}
			sql = sql.replaceAll(key, value.toString());
		}
	}

	int getCached() {
		return cached;
	}

	String getDb() {
		return db;
	}

	// !todo check these are used, remove if not
	String getSql() {
		return sql;
	}

	String[] getFields() {
		return fields;
	}

	boolean areErrorFree() {
		return errors.isEmpty();
	}

	String getCronSchedule() {
		return cronSchedule;
	}

	Format getFormat() {
		return format;
	}

	Csv getCsv() {
		return csv;
	}

	List<String> getErrors() {
		return errors;
	}

	String getLink() {
		@SuppressWarnings("unchecked")
		final Parameter<String> linkParameter = (Parameter<String>)parameters.get(LINK_KEY);
		return linkParameter == null ? null : linkParameter.value;
	}

	/**
	 * This is a special for {@link TimeLineChartRenderer} as it needs the size
	 * rendering as a style
	 * 
	 * @param sb
	 */
	void renderSizeAsStyle(StringBuilder sb) {
		final SizeParameter size = (SizeParameter)parameters.get(SIZE_KEY);
		size.setLimits(limits);
		size.toStyle(sb);
	}

	Collection<Parameter<?>> getParameters() {
		return parameters.values();
	}

	private static String getStringParameter(Map<String, String> parameters, String key) {
		final String result;
		final String string = parameters.get(key);
		if(string == null) {
			result = null;
		} else {
			final String trimmed = string.trim();
			result = trimmed.length() == 0 ? null : trimmed;
		}
		parameters.remove(key);
		return result;
	}

    private <T> T process(Parameter<T> parameter) {
		final T result;
		@SuppressWarnings("unchecked")
		final Parameter<T> p = (Parameter<T>)parameter.validate(plugin, inputParameters, errors);
		if(p == null) {
			if(parameter.alwaysAdd) {
				parameters.put(parameter.key, p);
			}
			result = null;
		} else {
			if(!p.neverAdd) {
				parameters.put(parameter.key, p);
			}
			result = p.value;
		}
		return result;
	}

	private int getIntegerValue(String key, int defaultValue) {
		final int result;
		final IntegerParameter parameter = (IntegerParameter)parameters.get(key);
		if(parameter == null) {
			result = defaultValue;
		} else {
			result = parameter.intValue();
		}
		return result;
	}

	/**
	 * Find the identifiers in a string. The variables are assumed to being the
	 * the form @ident@ where ident is int the java identifier format of
	 * "[a-zA-Z][a-zA-Z0-9_]*".
	 * 
	 * Warning: this is kind of limited as it doesn't take the context of the
	 * string into context - i.e something that isn't an identifier might appear
	 * as an identifier without knowing the language
	 * 
	 * @return null or list of at least one field names
	 */
	private String[] findFields(String string) {
		final Matcher matcher = identifierPattern.matcher(string);
		final List<String> fieldList = new ArrayList<String>();
		while(matcher.find()) {
			final int start = matcher.start();
			final int end = matcher.end();
			final String identifier = string.substring(start + 1, end - 1);
			if(!fieldList.contains(identifier)) {
				fieldList.add(identifier);
			}
		}
		return fieldList.size() == 0 ? null : fieldList.toArray(new String[fieldList.size()]);
	}
	
	Parameter<?> getParameter(String key) {
		return parameters.get(key);
	}

	public boolean hasSpreadsheet() {
		return parameters.get(SPREADSHEET_KEY) != null;
    }
}

