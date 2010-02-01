/*
	Copyright 2010 Inexas. All rights reserved.

	Licensed under the Inexas Software License V1.0. You may not use this file 
	except in compliance with the License. You may obtain a copy of the License
	at http://www.inexas.com/ISL-V1.0.

	Unless required by applicable law or agreed to in writing, software distributed 
	under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
	CONDITIONS OF ANY KIND, either express or implied. See the License for the 
	specific language governing permissions and limitations under the License.
*/
package com.inexas.pl.scheduler;

import java.io.*;
import java.util.*;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.providers.*;
import com.inexas.util.*;

/**
 * The Scheduler is a handles scheduled Jobs.
 * 
 * The Scheduler is a background thread that manages the running of scheduled
 * jobs. During the page saving process, {@link SchedulerFilter} checks through
 * the page looking for {@link Schedulable}s. All jobs for the page are first
 * removed from the Job queue and the newly 'discovered' tasks are added.
 * 
 * We keep two pages updated: a log page and a console. The console page
 * reflects what the Scheduler is doing whilst the log page keeps a record of
 * what it has done.
 * 
 * The current version assumes that the page management is not transactionally
 * secure (which it is not) a maintains a 'scavenger' thread to keep the job
 * queue in line with the pages. When we go to a database then we can improve
 * this situation drastically.
 * 
 * todo Scheduler + ScheduleFilter should be code reviewed -> simplify
 */
public class Scheduler {
	public final static String schedulerPage = "Job Scheduling";
	private final Queue<Job> jobQueue = new PriorityQueue<Job>(16, new JobComparator());
	private final Timer jobTimer = new Timer("Job timer");
	private final Timer scavengerTimer = new Timer("Scheduler scavenger");
	private final Set<JobRunner> runningJobs = new HashSet<JobRunner>();
	private Map<String, String> scheduleNameMap;
	private static Scheduler instance;
	private final File log, console;
	private final WikiEngine engine;
	private JobRunner headOfQueueThread;
	

	private class JobComparator implements Comparator<Job> {

		public int compare(Job lhs, Job rhs) {
			final long lhsRunTime = lhs.getNextRunTime();
			assert lhsRunTime > 0;
			final long rhsRumTime = rhs.getNextRunTime();
			assert rhsRumTime > 0;
			return lhsRunTime < rhsRumTime ? -1 : 1;
		}

	}

	private class JobRunner extends TimerTask {
		final Job job;
		private Date startedAt;

		public JobRunner(Job job) {
			this.job = job;
		}

		@Override
		public void run() {
			// This thread was the job runner but recycle it to run the job.
			// Take the job from the queue and add it to the running jobs set.
			synchronized(jobQueue) {
				headOfQueueThread = null;
				jobQueue.remove();
				startedAt = new Date();
				runningJobs.add(this);
				scheduleNextJob();
				updateConsole();
			}

			// This might take a long time...
			job.run();
			updateLog(this);
			startedAt = null;

			// The job is finished re-queue it
			synchronized(jobQueue) {
				runningJobs.remove(this);
				add(job);
				updateConsole();
			}
		}

		public Date getStartedAt() {
			return startedAt;
		}

	}

	public static void initialize(WikiEngine wikiEngine, Properties properties) {
		instance = new Scheduler(wikiEngine, properties);
	}

	public static Scheduler getInstance() {
		return instance;
	}

	private Scheduler(WikiEngine wikiEngine, Properties properties) {
		this.engine = wikiEngine;
		final String home = properties.getProperty("jspwiki.fileSystemProvider.pageDir");
		log = new File(home + "/SiteSchedulerLog.txt");
		console = new File(home + "/SiteSchedulerConsole.txt");
		
		updateNamedSchedules(wikiEngine);

		scavengerTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				reload();
			}
		},
		10 * 1000, // Delay in ms before first run
		6 * 60 * 60 * 1000); // Run every 6 hours 
	}

	Scheduler() {
		engine = null;
		log = console = null;
		// Unit testing only
	}

	public void add(Job job) {
		// !todo think about what happens if the page is updated..
		synchronized(jobQueue) {
			if(job.setNextRunTime()) {
				jobQueue.add(job);

				// Is the new job head of queue, if so it needs to be run
				// next...
				if(jobQueue.peek() == job) {
					if(headOfQueueThread != null) {
						headOfQueueThread.cancel();
						headOfQueueThread = null;
					}
					scheduleNextJob();
				}
			}
			updateConsole();
		}
	}

	public void remove(Job job) {
		synchronized(jobQueue) {
			if(jobQueue.contains(job)) {
				final Job headOfQueue = jobQueue.peek();
				jobQueue.remove(job);
				if(headOfQueue == job) {
					headOfQueueThread.cancel();
					scheduleNextJob();
				}
			} else {
				boolean found = false;
				for(final JobRunner jobRunner : runningJobs) {
					if(jobRunner.job.equals(job)) {
						runningJobs.remove(jobRunner);
						jobRunner.cancel();
						found = true;
						break;
					}
				}
				if(!found) {
					// todo log a warning
				}
			}
			updateConsole();
		}
	}

	public void terminate() {
		for(final JobRunner job : runningJobs) {
			job.cancel();
		}
	}

	private void scheduleNextJob() {
		if(headOfQueueThread == null && !jobQueue.isEmpty()) {
			final Job job = jobQueue.peek();
			final long delay = job.getDelay();
			headOfQueueThread = new JobRunner(job);
			jobTimer.schedule(headOfQueueThread, delay < 0 ? 500 : delay);
		}
	}

	Queue<Job> getJobQueue() {
		return jobQueue;
	}

	private void updateConsole() {
		// todo This would be nice to do just in time...
		try {
			final FileWriter writer = new FileWriter(console);
			final BufferedWriter out = new BufferedWriter(writer);
			final StringBuilder sb = new StringBuilder();
			write(sb, "!!!Currently running jobs\n||Started||Page:version||Task\n", runningJobs, true);
			write(sb, "!!!Currently idle jobs\n||Started||Page:version||Task\n", jobQueue, false);
			out.write(sb.toString());
			out.close();
		} catch(final Exception e) {
			throw new RuntimeException("Error writing scheduler console", e);
		}
	}

	private void write(StringBuilder sb, String header, Collection<?> jobs, boolean isRunning) {
		sb.append(header);
		for(final Object object : jobs) {
			final Job job = isRunning ? ((JobRunner)object).job : ((Job)object);
			sb.append('|');
			final Date date;
			if(isRunning) {
				date = ((JobRunner)object).getStartedAt();
			} else {
				date = new Date(job.getNextRunTime());
			}
			sb.append(DateUtilities.toDateTimeString(date));
			sb.append('|');
			sb.append(job.getPageName());
			sb.append(':');
			sb.append(job.getPageVersion());
			sb.append('|');
			sb.append(job.toString());
			sb.append('\n');
		}
	}

	private void updateLog(JobRunner jobRunner) {
		try {
			// If the file got to big, delete it
			// todo Better scheme than this, e.g. last 3 run times per job
			final boolean append = log.length() < 10 * 1000;
			final FileWriter writer = new FileWriter(log, append);
			final BufferedWriter sb = new BufferedWriter(writer);

			if(!append) {
				sb.append("||Start||End||Page:version||Task\n");
			}
			sb.append('|');
			sb.append(DateUtilities.toDateTimeString(jobRunner.getStartedAt()));
			sb.append('|');
			sb.append(DateUtilities.toDateTimeString(new Date()));
			sb.append('|');
			final Job job = jobRunner.job;
			sb.append(job.getPageName());
			sb.append(':');
			sb.append(Integer.toString(job.getPageVersion()));
			sb.append('|');
			sb.append(job.toString());
			sb.append('\n');

			sb.close();
		} catch(final Exception e) {
			throw new RuntimeException("Error writing scheduler console", e);
		}
	}

	public Collection<Job> getAllJobs() {
		// !todo Read up on synchronization!!!
		final Collection<Job> result = new ArrayList<Job>();
		synchronized(jobQueue) {
			result.addAll(jobQueue);
			for(final JobRunner jobRunner : runningJobs) {
				result.add(jobRunner.job);
			}
		}
		return result;
	}

	/**
	 * This is run during startup and every two hours or so.
	 * 
	 * For each page, parse the page to get the scheduled jobs, query the job
	 * queues to find out what is actually scheduled, If there is a difference
	 * then correct it.
	 * 
	 * todo This is not thread safe
	 * todo We could also make this better by remembering the page versions
	 */
	private void reload() {
		try {
			final PageManager pageManager = engine.getPageManager();
			@SuppressWarnings("unchecked")
			final Iterator<WikiPage> pages = pageManager.getAllPages().iterator();
			while(pages.hasNext()) {
				final WikiPage page = pages.next();
				final String pageName = page.getName();
				final WikiContext context = new WikiContext(engine, page);
				final String wikiText = context.getEngine().getPureText(page);
				final List<Job> shouldBe = SchedulerFilter.getJobs(context, wikiText);
				synchronized(jobQueue) {
					final Collection<Job> actual = getAllJobs(pageName);
					// Compare actual with shouldBe. Ideally they would
					// both be sets so we can do and equals, but they're
					// not. The following codes assumes that Job.equals()
					// is implemented properly
					boolean equal;
					if(actual.size() == shouldBe.size()) {
						equal = true;
						for(final Job job : actual) {
							if(!shouldBe.contains(job)) {
								equal = false;
								break;
							}
						}
					} else {
						equal = false;
					}
					if(!equal) {
						// !todo Log me
						removeJobsForPage(pageName);
						for(final Job job : shouldBe) {
							add(job);
						}
					}
				}
			}
		} catch(final ProviderException e) {
			throw new RuntimeException("Error starting scheduler", e);
		}
	}

	private Collection<Job> getAllJobs(String pageName) {
		final Collection<Job> result = new ArrayList<Job>();
		for(final Job job : jobQueue) {
			if(job.getPageName().equals(pageName)) {
				result.add(job);
			}
		}
		for(final JobRunner jobRunner : runningJobs) {
			final Job job = jobRunner.job;
			if(job.getPageName().equals(pageName)) {
				result.add(job);
			}
		}
		return result;
    }
	
	public void removeJobsForPage(String pageName) {
		final Scheduler scheduler = Scheduler.getInstance();
		for(final Job job : scheduler.getAllJobs()) {
			if(job.getPageName().equals(pageName)) {
				scheduler.remove(job);
			}
		}
	}

	public void updateNamedSchedules(WikiEngine wikiEngine) {
		// todo This needs to check for jobs already running jobs, in fact the whole dependencies thing
		scheduleNameMap = new NameValueMap(wikiEngine, schedulerPage).getMap();
    }

	public String getNamedSchedule(String name) {
		return scheduleNameMap.get(name);
    }
	
}
