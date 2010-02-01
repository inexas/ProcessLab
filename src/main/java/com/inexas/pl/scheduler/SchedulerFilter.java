package com.inexas.pl.scheduler;

import java.io.*;
import java.util.*;
import org.jdom.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.event.*;
import com.ecyrd.jspwiki.filters.*;
import com.ecyrd.jspwiki.parser.*;
import com.ecyrd.jspwiki.plugin.*;
import com.ecyrd.jspwiki.plugin.PluginManager.*;

/**
 * This filter takes care of scheduling.
 * 
 * The code uses {@link ReferenceManager} as a model.
 * 
 * Our job is to trap the saving and deletion of pages. When pages are saved we
 * scan them for scheduled items. We then update the schedule accordingly. When
 * pages are deleted we tell the scheduler.
 * 
 * todo Make this transactional. The shortcoming is with this filtering thing is
 * that it's not transactional. If the page is saved and, for any reason, this
 * code is not called then the plugin will never get scheduled. It's up to the
 * user to figure out something has gone wrong. This will be very difficult to
 * do while we are still using JSPwiki.
 * 
 */
public class SchedulerFilter extends BasicPageFilter implements WikiEventListener {
	private static SchedulerFilter instance;

	public static SchedulerFilter getIntance() {
		return instance;
	}
	
	public SchedulerFilter() {
		instance = this;
    }
	
	/**
	 * Called during {@link WikiEngine} initialization.
	 * 
	 * @param wikiEngine
	 * 
	 * @param pages
	 */
	public void initialize(WikiEngine wikiEngine, @SuppressWarnings("unused") ArrayList<WikiPage> pages) {
		m_engine = wikiEngine;
		WikiEventUtils.addWikiEventListener(
		        m_engine.getPageManager(),
		        WikiPageEvent.PAGE_DELETED,
		        this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(WikiEvent event) {
		if((event instanceof WikiPageEvent) && (event.getType() == WikiPageEvent.PAGE_DELETED)) {
			final String pageName = ((WikiPageEvent)event).getPageName();

			if(pageName != null) {
				pageDeleted(pageName);
			}
		}
	}
	
	/**
	 * After the page has been saved, updates the reference lists.
	 * 
	 * @param context
	 *            {@inheritDoc}
	 * @param content
	 *            {@inheritDoc}
	 */
	public void postSave(WikiContext context, String content) {
		pageUpdated(context, content);
	}

	/*
	 * The following methods are called from key places in the
	 * rest of the code. They call the helper methods to create
	 * and remove jobs accordingly
	 */

	public void pageCreated(WikiContext context) {
		final WikiPage page = context.getPage();
		final String wikiText = context.getEngine().getPureText(page);
		createJobsForPage(context, wikiText);
	}
	
	public void pageUpdated(WikiContext context, String wikiText) {
		final WikiPage page = context.getPage();
		Scheduler.getInstance().removeJobsForPage(page.getName());
		createJobsForPage(context, wikiText);
		if(Scheduler.schedulerPage.equals(page.getName())) {
			Scheduler.getInstance().updateNamedSchedules(context.getEngine());
		}
	}

	public void pageDeleted(String pageName) {
		Scheduler.getInstance().removeJobsForPage(pageName);
	}

	private void createJobsForPage(WikiContext context, String wikiText) {
		final List<Job> jobs = getJobs(context, wikiText);
		final Scheduler scheduler = Scheduler.getInstance();
		for(final Job job : jobs) {
			scheduler.add(job);
		}
	}

	public static List<Job> getJobs(WikiContext context, String wikiText) {
		try {
			final List<Job> result = new ArrayList<Job>();
			// Parse the new text...
	        final JSPWikiMarkupParser parser = new JSPWikiMarkupParser(context, new StringReader(wikiText));
	        final WikiDocument document = parser.parse();
	        
	        // Inspect the document looking for plugins...
			final Element root = document.getRootElement();
			final PluginManager pluginManager = context.getEngine().getPluginManager();
			visit(context, root, pluginManager, result);
			return result;
        } catch(final IOException e) {
        	throw new RuntimeException("Error parsing page" + context.getPage().getName());
        }
	}

	/**
	 * Recursively visit the wiki document and return all jobs from any plugins
	 * the document might contain.
	 * 
	 * @param element
	 * @param jobs
	 */
    private static void visit(WikiContext context, Element element, PluginManager pluginManager, List<Job> jobs) {
    	@SuppressWarnings("unchecked")
		final Iterator<Content> contents = element.getContent().iterator();
    	while(contents.hasNext()) {
    		final Content content = contents.next();
    		if(content instanceof PluginContent) {
    			final PluginContent pluginContent = (PluginContent)content;
    			final WikiPluginInfo info = pluginManager.getPluginInfo(pluginContent.getPluginName());
                try {
           			final WikiPlugin plugin = info.newPluginInstance();
           			if(plugin instanceof HasJobs) {
           				final HasJobs hasJobs = (HasJobs)info.newPluginInstance();
           				@SuppressWarnings("unchecked")
           				final Map<String, String> map = pluginContent.getParameters();
           				final Job job = hasJobs.getJobs(context, map);
           				if(job != null) {
           					jobs.add(job);
           				}
           			}
                } catch(final Exception e) {
                	throw new RuntimeException("Error loading plugin class", e);
                }
			} else if(content instanceof Element) {
				visit(context, (Element)content, pluginManager, jobs);
			}
    	}
    }

}
