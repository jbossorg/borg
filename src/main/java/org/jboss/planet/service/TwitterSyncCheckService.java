/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import org.jboss.planet.model.PostStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for checking not synced content to Twitter. It's {@link java.util.Timer} based checker and thus runs in
 * separate thread
 *
 * @author Libor Krzyzanek
 */
@Named
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class TwitterSyncCheckService {

	@Resource
	private TimerService timerService;

	@Inject
	private Logger log;

	@Inject
	private TwitterService twitterService;

	@Inject
	private PostService postService;

	@Inject
	private ConfigurationService configurationService;

	/**
	 * Initial start after app startup
	 */
	public static final int STARTUP_DELAY_MIN = 20;

	/**
	 * Number of failed tries stops execution
	 */
	public static final int FAIL_THRESHOLD = 5;

	@PostConstruct
	public void start() {
		log.log(Level.INFO, "Initiating the first Twitter Sync check in {0} min", STARTUP_DELAY_MIN);
		timerService.createSingleActionTimer(STARTUP_DELAY_MIN * 60 * 1000, new TimerConfig(null, false));
	}

	private void initTimer() {
		int intervalInSec = configurationService.getConfiguration().getUpdateInterval();
		log.log(Level.INFO, "Initiating next Twitter Sync in {0} min", intervalInSec / 60);
		timerService.createSingleActionTimer(intervalInSec * 1000, new TimerConfig(null, false));
	}

	@SuppressWarnings("unused")
	@Timeout
	public void checkPostsToSync() {
		if (!configurationService.getConfiguration().isTwitterEnabled()) {
			log.log(Level.INFO, "Sync to Twitter is disabled");
			initTimer();
			return;
		}
		log.log(Level.INFO, "Sync to Twitter started");

		// Getting only IDs to avoid "no session" on lazy initialization
		List<Integer> postsToSync = postService.find(PostStatus.SYNCED);

		int shortURLLength;
		Twitter twitter = twitterService.createTwitterClient();
		try {
			shortURLLength = twitter.help().getAPIConfiguration().getShortURLLength();
		} catch (TwitterException e) {
			shortURLLength = TwitterService.TWITTER_SHORT_URL_LENGTH_DEFAULT;
		}

		int syncSuccess = 0;
		int syncFail = 0;

		for (Integer id : postsToSync) {
			if (syncFail > FAIL_THRESHOLD && syncSuccess == 0) {
				// Something is probably badly configured. Let's skip this update.
				log.log(Level.SEVERE, "{0} attempts sync to Twitter failed. Aborting.", FAIL_THRESHOLD);
				break;
			}
			boolean success = twitterService.syncPost(id, twitter, shortURLLength);
			if (success) {
				syncSuccess++;
			} else {
				syncFail++;
			}
		}

		log.log(Level.INFO, "Twitter Sync completed. Tweets posted: {0}, failed: {1}", new Integer[]{syncSuccess, syncFail});

		initTimer();
	}

}
