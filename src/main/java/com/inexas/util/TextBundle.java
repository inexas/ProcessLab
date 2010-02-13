package com.inexas.util;

import java.io.*;
import java.util.*;
import java.util.jar.*;


/**
 * This class allows a set of property files to be used like resource bundles 
 * for SQL or other similar applications where there are several dialects or 
 * versions of a single dialect. An example is worth a thousand words:</p>
 * <pre>
 * sql.properties====
 * # one might expect an SQL 92 definition here
 * select=select * from mytable
 * insert=insert into tab(id,name)values('%s',%d)
 * ==================
 * 
 * sql_db2.properties====
 * # here's the version for the DB2 database
 * select=SELECT * FROM DB2TAB
 * ==================
 * </pre>
 * An application would construct a TextBundle using "sql_db2"
 * as the name. On loading the properties file this class would 
 * discover that a predecessor existed and load that. The property files
 * are used in the load order to service requests.
 * 
 * @author Keith Whittingham
 * @version $Revision: 1.1 $
 */
public class TextBundle extends Properties {
	// todo JUnit me
	private static final long serialVersionUID = 1832358303485280887L;
	private String baseName;

	/**
	 * Load a set of properties as a text bundle. The base name is referenced
	 * from the home directory as retrieved from Config and may include a 
	 * directory structure. E.g. a base name of config/ab_cd_ef will cause a 
	 * search of config/ab.properties, config/ab_cd.properties and 
	 * config/ab_cd_ef.properties to be searched for and loaded if present.
	 * 
	 * @param baseName the base name of the text bundle
	 */
	public TextBundle(String baseName) {
		assert baseName != null && baseName.length() > 0;
		
		this.baseName = baseName;
		
		final Collection<String> filenames = generateFilenames(baseName);
		if(filenames.size() == 0) {
			throw new RuntimeException("No text bundle for base name: " + baseName);
		}
		
		boolean isFirst = true;
		final Iterator<String> i = generateFilenames(baseName).iterator();
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String filename = null;
		Properties properties = null;
		while(i.hasNext()) {
			try {
				filename = i.next();
				final InputStream is = loader.getResourceAsStream(filename);
				if(is == null) {
					throw new FileNotFoundException(filename);
				}
				properties = process(is, i.hasNext(), properties);
				is.close();
				isFirst = false;
			} catch(final FileNotFoundException e) {
				if(isFirst) {
					throw new RuntimeException("No text bundle for filename: " + filename, e);
				}
				// ignore files not found...
			} catch(IOException e) {
				throw new RuntimeException("Error loading file: " + filename, e);
			}
		}
	}

	/**
	 * The text bundle name is assumed to be in the format "name{_dlt]*"
	 * where name is the base name of the resource bundle and dlt is a
	 * specialization in a similar way to language resource bundles. If the name
	 * is "config/abc_def_ghi" is passed then we attempt to load three
	 * properties files: abc.properties, abc_def.properties and abc_def_ghi.
	 * properties.
	 *  
	 * @param jarFilename the name of the JAR file to read
	 * @param baseName the resource bundle name to read in
	 */
	public TextBundle(String jarFilename, String baseName) {
		boolean found = false;
		
		this.baseName = baseName;
		try {
			// tokenize the class path and look for the right file name...
			String classpath = System.getProperty("java.class.path");
			String pathSeparator = System.getProperty("path.separator");
			StringTokenizer st = new StringTokenizer(classpath, pathSeparator);
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (s.endsWith(jarFilename)) {
					found = true;
					// got it, create the jarfile...
					JarFile jf = new JarFile(s);

					// generate a list of jar entries...
					List<JarEntry> jarEntries = new ArrayList<JarEntry>();
					Iterator<String> i = generateFilenames(baseName).iterator();
					while (i.hasNext()) {
						String filename = i.next();
						// try and load the properties file for this one...
						JarEntry je = jf.getJarEntry(filename);
						if (je != null) {
							jarEntries.add(je);
						}
					}

					// now process the jar entries....
					Properties properties = null;
					JarEntry je = null;
					Iterator<JarEntry> j = jarEntries.iterator();
					while (j.hasNext()) {
						je = j.next();
						try {
							final InputStream is = jf.getInputStream(je);
							properties = process(is, i.hasNext(), properties);
							is.close();
						} catch (FileNotFoundException e) {
							// ignore files not found...
						} catch (IOException e) {
							throw new RuntimeException(
									"Error loading jar entry: " + je.getName(), e);
						}
					}
					jf.close();
				}
			}
			if(!found) {
				// test
				//File wFile = new File("abcdefg");
//				FileWriter fw = new FileWriter("abcdefg");
//				fw.write("hello Keith");
//				fw.close();
//				Iterator i = generateFilenames("/sql").iterator();
//				Properties properties = null;
//				while (i.hasNext()) {
//					String filename = null;
//					try {
//						filename = (String)i.next();
//						InputStream is = new FileInputStream(filename);
//						properties = process(is, i.hasNext(), properties);
//						is.close();
//					} catch (FileNotFoundException e) {
//						// ignore files not found...
//					} catch (IOException e) {
//						throw new ProcessLabRuntimeException("Error loading file: " + filename, e);
//					}
//				}



//				// if we didn't found the jarfile so far we try as last chance
//				// to load the properties like this (fix for tomcat)
				System.out.println("loading sql properties from classpath...:"+baseName+".properties");
				final ResourceBundle rb = ResourceBundle.getBundle(baseName);
				final Enumeration<String> en = rb.getKeys();
				while(en.hasMoreElements()){
					String key = en.nextElement();
					this.put(key,rb.getString(key));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error accessing JAR file: " + jarFilename, e);
		}
	}

	private Properties process(InputStream is, boolean notLast, Properties properties)
	throws IOException {
		final Properties returnValue;
		//   [first] <--defaults-- [ ... ] <--defaults-- [last=this]
		if (properties == null) {
			if (notLast) {
				// first, but not last...
				returnValue = new Properties();
			} else {
				// first *and* last...
				returnValue = this;
			}
		} else {
			if (notLast) {
				// not first or last...
				returnValue = new Properties(properties);
			} else {
				// not first but last... 
				defaults = properties;
				returnValue = this;
			}
		}
		returnValue.load(is);
		return properties;
	}

	private List<String> generateFilenames(String theBaseName) {
		List<String> list = new ArrayList<String>();
		// split up the baseName into path and filename and tokenize the latter...
		int slash = theBaseName.lastIndexOf('\\');
		int backSlash = theBaseName.lastIndexOf('/');
		int pathLength = 1 + slash > backSlash ? slash : backSlash;
		String path, filename;
		if(pathLength == -1) {
			path = "";
			filename = theBaseName;
		} else {
			path = theBaseName.substring(0, pathLength);
			filename = theBaseName.substring(pathLength);
		}
		StringTokenizer st = new StringTokenizer(filename, "_");

		String name = path + st.nextToken();
		list.add(name + ".properties");
		while (st.hasMoreTokens()) {
			name += "_" + st.nextToken();
			list.add(name + ".properties");
		}
		return list;
	}

	public String getString(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}
	
	public String getString(String key) {
		final String value = super.getProperty(key);
		if(value == null) {
			throw new RuntimeException("Missing key in TextBundle " + baseName + " key: " + key);
		}
		return value;
	}

	@Deprecated
	@Override
	public String getProperty(String key) {
		return super.getProperty(key);
	}

}
