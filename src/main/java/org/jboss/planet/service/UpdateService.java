/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.planet.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

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

    @Inject
    protected EmbeddedCacheManager container;

    @Resource
    TimerService timerService;

    public static final String CACHE_NAME = "sync-feeds";

    public static final String WORKERS_COUNT = "borg.updatefeed.workers.count";


    protected Cache<Integer, Date> feedsToSync;

    private Set<UpdateFeedsExecutor> threadPool;

    @PostConstruct
    public void init() {
        feedsToSync = container.getCache(CACHE_NAME);

        int threadsCount = Integer.parseInt(System.getProperties().getProperty(WORKERS_COUNT, "2"));
        int executorIntervalInSec = 60;
        threadPool = new HashSet<>(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            log.log(Level.INFO, "Initializing Update Feeds Executor #" + (i + 1));
            UpdateFeedsExecutor thread = new UpdateFeedsExecutor(feedsService, parserService, mergeService, feedsToSync, i + 1, executorIntervalInSec);
            thread.setContextClassLoader(this.getClass().getClassLoader());
            thread.setDaemon(true);
            thread.start();

            threadPool.add(thread);
        }

        // First update fired 0 minutes after server startup. Executors process them during next run.
        int startupInMin = 0;
        log.log(Level.INFO, "Initializing first blog posts update in {0} min", startupInMin);
        timerService.createSingleActionTimer(startupInMin * 60 * 1000, new TimerConfig(null, false));
    }

    @PreDestroy
    public void close() {
        if (threadPool == null) {
            return;
        }

        for (UpdateFeedsExecutor thread : threadPool) {
            thread.stopExecution();
        }
    }

    private void initTimer() {
        int intervalInSec = configurationService.getConfiguration().getUpdateInterval();
        log.log(Level.INFO, "Initializing next blog posts update in {0} min", intervalInSec / 60);
        timerService.createSingleActionTimer(intervalInSec * 1000, new TimerConfig(null, false));
    }

    @SuppressWarnings("unused")
    @Timeout
    public void updateFeeds() {
        List<Integer> feeds = feedsService.getAcceptedFeedIds();
        log.log(Level.INFO, "Put {0} feeds for update by independent workers.", feeds.size());

        for (int id : feeds) {
            feedsToSync.put(id, new Date());
        }

        initTimer();
    }

}
