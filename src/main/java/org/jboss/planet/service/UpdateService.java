/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.planet.event.MergePostsEvent;
import org.jboss.planet.exception.ParserException;
import org.jboss.planet.exception.UpdateException;
import org.jboss.planet.model.RemoteFeed;
import org.jboss.planet.util.GeneralTools;

/**
 * Service handle update feeds
 *
 * @author Libor Krzyzanek
 */
@Named
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class UpdateService {

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private FeedsService feedsService;

	@Inject
	private ParserService parserService;

	@Inject
	private MergeService mergeService;

	@Inject
	private Logger log;

	@Resource
	TimerService timerService;

	private long lastUpdateStart;
	private long lastUpdateEnd;

	private AtomicBoolean updateInProgress;

	private List<Exception> globalExceptions;
	private Map<String, List<UpdateException>> feedUpdateExceptions;

	@Inject
	private Event<MergePostsEvent> mergeStatEvent;

	@PostConstruct
	public void start() {
		// First update fired 1 minutes after server startup
		int startupInMin = 1;
		log.log(Level.INFO, "Initializing first blog posts update in {0} min", startupInMin);
		timerService.createSingleActionTimer(startupInMin * 60 * 1000, new TimerConfig(null, false));
	}

	private void initTimer() {
		int intervalInSec = configurationService.getConfiguration().getUpdateInterval();
		log.log(Level.INFO, "Initializing next blog posts update in {0} min", intervalInSec / 60);
		timerService.createSingleActionTimer(intervalInSec * 1000, new TimerConfig(null, false));
	}

	@SuppressWarnings("unused")
	@Timeout
	public void updateFeeds() {
		updateInProgress = new AtomicBoolean(true);
		log.info("Update Feeds started");

		globalExceptions = new ArrayList<>();
		feedUpdateExceptions = new LinkedHashMap<>();

		int newPosts = 0;
		int mergedPosts = 0;
		int totalPosts = 0;
		int ignoredPosts = 0;
		int duplicateTitles = 0;

		List<RemoteFeed> feeds = feedsService.getAcceptedFeeds();
		int counter = 0;
		int totalCount = feeds.size();
		for (RemoteFeed feed : feeds) {
			counter++;
			String progress = counter + "/" + totalCount;

			boolean canContinue = feedsService.checkUpdateFails(feed);
			if (!canContinue) {
				if (log.isLoggable(Level.INFO)) {
					log.info("Update Feed (" + progress + ") for '" + feed.getName() + "' is disabled.");
				}
				continue;
			}

			try {
				RemoteFeed parsedFeed = parserService.parse(feed.getRemoteLink(), null, null);
				MergePostsEvent stat = mergeService.mergePosts(feed, parsedFeed.getPosts());

				if (log.isLoggable(Level.INFO)) {
					log.info("Update Feed (" + progress + ") for '" + feed.getName() + "' finished. "
							+ "STATS: new: " + stat.getNewPosts()
							+ ", merged: " + stat.getMergedPosts()
							+ ", ignored: " + stat.getIgnoredPosts()
							+ ", duplicate titles: " + stat.getDuplicateTitles()
							+ ", total: " + stat.getTotalPosts());
				}

				newPosts += stat.getNewPosts();
				mergedPosts += stat.getMergedPosts();
				totalPosts += stat.getTotalPosts();
				ignoredPosts += stat.getIgnoredPosts();
				duplicateTitles += stat.getDuplicateTitles();

				// reset update fail counter
				if (feed.getUpdateFailCount() != null && feed.getUpdateFailCount() > 0) {
					feed.setUpdateFailCount(0);
					feedsService.update(feed, false);
				}
			} catch (ParserException e) {
				String message = "Problem during parsing feed  (" + progress + "): "
						+ feed.getName() + ", cause type: " + e.getCauseType()
						+ ", parse fails: " + feed.getUpdateFailCount();
				if (ParserException.CAUSE_TYPE.CONNECTION_PROBLEM.equals(e.getCauseType())) {
					log.log(Level.SEVERE, message + ", Exception message: ", e.getMessage());
				} else {
					log.log(Level.SEVERE, message, e);
				}

				addFeedUpdateException(feed.getName(), new UpdateException(e));
				feed.incrementUpdateFailCount();
				feedsService.update(feed, false);
			}
		}

		log.info("Update all feeds finished. "
				+ "Total feeds: " + totalCount
				+ ", processed feeds: " + counter
				+ ", new posts: " + newPosts
				+ ", Merged posts: " + mergedPosts
				+ ", Ignored posts: " + ignoredPosts
				+ ", Duplicate titles: " + duplicateTitles
				+ ", Total posts: " + totalPosts);

		updateInProgress = new AtomicBoolean(false);

		mergeStatEvent.fire(new MergePostsEvent(newPosts, mergedPosts, totalPosts, ignoredPosts, duplicateTitles));

		initTimer();
	}

	public void addFeedUpdateException(String feedName, UpdateException exception) {
		List<UpdateException> exceptions = feedUpdateExceptions.get(feedName);
		if (exceptions == null) {
			exceptions = new ArrayList<>();
			feedUpdateExceptions.put(feedName, exceptions);
		}

		if (exceptions.size() < 3) {
			exceptions.add(exception);
		}
	}

	public void addGlobalException(Exception exception) {
		if (globalExceptions.size() < 3) {
			globalExceptions.add(exception);
		}
	}

	public List<UpdateException> getFeedUpdateExceptionsForFeed(String feedName) {
		return feedUpdateExceptions.get(feedName);
	}

	public List<String> getFeedUpdateExceptionNames() {
		return new ArrayList<String>(feedUpdateExceptions.keySet());
	}

	public void clearFeedsExceptions() {
		feedUpdateExceptions.clear();
	}

	public List<Exception> getGlobalExceptions() {
		return globalExceptions;
	}

	public void clearGlobalExceptions() {
		globalExceptions.clear();
	}

	public long getLastUpdateEnd() {
		return lastUpdateEnd;
	}

	public void setLastUpdateEnd(long lastUpdateEnd) {
		this.lastUpdateEnd = lastUpdateEnd;
	}

	public String getLastUpdateEndDate() {
		return DateFormat.getDateTimeInstance().format(getLastUpdateEnd());
	}

	public long getLastUpdateStart() {
		return lastUpdateStart;
	}

	public void setLastUpdateStart(long lastUpdateStart) {
		this.lastUpdateStart = lastUpdateStart;
	}

	public String getLastUpdateStartDate() {
		return DateFormat.getDateTimeInstance().format(getLastUpdateStart());
	}

	public AtomicBoolean getUpdateInProgress() {
		return updateInProgress;
	}

	public String getNow() {
		return DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
	}

	public int getUpdateInterval() {
		return configurationService.getConfiguration().getUpdateInterval();
	}

	// @Restrict("#{identity.hasPermission('admin', null)}")
	public void setUpdateInterval(int updateInterval) {
		configurationService.getConfiguration().setUpdateInterval(updateInterval);
	}

	public int getConnectionTimeout() {
		return configurationService.getConfiguration().getConnectionTimeout();
	}

	// @Restrict("#{identity.hasPermission('admin', null)}")
	public void setConnectionTimeout(int connectionTimeout) {
		configurationService.getConfiguration().setConnectionTimeout(connectionTimeout);
	}

	public int getReadTimeout() {
		return configurationService.getConfiguration().getReadTimeout();
	}

	// @Restrict("#{identity.hasPermission('admin', null)}")
	public void setReadTimeout(int readTimeout) {
		configurationService.getConfiguration().setReadTimeout(readTimeout);
	}

	public String getExceptionStackTrace(Exception e) {
		return GeneralTools.getExceptionStackTrace(e);
	}

	// @Restrict("#{identity.hasPermission('admin', null)}")
	public void save() {
		// restartUpdateThread();
		// facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "blog.configuration.saved");
	}
}
