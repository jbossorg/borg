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
import java.util.Timer;
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
import javax.management.ObjectName;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.planet.model.PostStatus;
import org.jboss.planet.util.MBeanUtils;

/**
 * Service for checking not synced content to dcp.jboss.org. It's {@link Timer} based checker and thus runs in
 * separate thread
 *
 * @author Libor Krzyzanek
 */
@Named
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
public class JBossSyncCheckService implements JBossSyncCheckServiceMBean {

    @Resource
    private TimerService timerService;

    @Inject
    private Logger log;

    @Inject
    private JBossSyncService jbossSyncService;

    @Inject
    private PostService postService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    protected EmbeddedCacheManager container;

    protected Cache<Integer, PostStatus> postsToSync;

    public static final String CACHE_NAME = "sync-feeds-to-dcp";

    public static final String WORKERS_COUNT = "borg.sync.dcp.workers.count";

    private Set<JBossSyncExecutor> threadPool;

    protected ObjectName mBeanName;

    protected Date lastUpdateDate;

    @PostConstruct
    public void init() {
        postsToSync = container.getCache(CACHE_NAME);

        int threadsCount = Integer.parseInt(System.getProperties().getProperty(WORKERS_COUNT, "2"));
        int executorIntervalInSec = 60;
        threadPool = new HashSet<>(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            log.log(Level.INFO, "Initializing Sync to DCP Executor #" + (i + 1) + " with execution interval: " + executorIntervalInSec + "s");
            JBossSyncExecutor thread = new JBossSyncExecutor(i + 1, jbossSyncService, executorIntervalInSec, postsToSync);
            thread.setContextClassLoader(this.getClass().getClassLoader());
            thread.setDaemon(true);
            thread.start();

            threadPool.add(thread);
        }

        // First update fired 5 minutes after server startup
        int startupInMin = 5;
        log.log(Level.INFO, "Initiating the first posts to sync to DCP check in {0} min", startupInMin);
        timerService.createSingleActionTimer(startupInMin * 60 * 1000, new TimerConfig(null, false));

        mBeanName = MBeanUtils.registerMBean(this, "org.jboss.planet:type=DCPSyncCheckService");
    }

    private void initTimer() {
        int intervalInSec = configurationService.getConfiguration().getUpdateInterval();
        log.log(Level.INFO, "Initiating next posts to sync to DCP in {0} min", intervalInSec / 60);
        timerService.createSingleActionTimer(intervalInSec * 1000, new TimerConfig(null, false));
    }

    @SuppressWarnings("unused")
    @Timeout
    public void checkPostsToSync() {
        log.log(Level.INFO, "Sync posts to DCP signed via {0}", configurationService.getConfiguration()
                .getSyncServer());
        // Getting only IDs to avoid "no session" on lazy initialization
        List<Integer> postsCreated = postService.find(PostStatus.CREATED);

        for (Integer id : postsCreated) {
            postsToSync.put(id, PostStatus.SYNCED);
        }
        List<Integer> postsResync = postService.find(PostStatus.FORCE_SYNC);
        for (Integer id : postsResync) {
            postsToSync.put(id, PostStatus.RESYNCED);
        }

        lastUpdateDate = new Date();

        log.log(Level.INFO, "Posts to sync: {0}. Resync: {1}", new Integer[] { postsCreated.size(), postsResync.size() });

        initTimer();
    }

    @PreDestroy
    public void close() {
        if (threadPool != null) {
            for (JBossSyncExecutor thread : threadPool) {
                thread.stopExecution();
            }
        }

        MBeanUtils.unregisterMBean(mBeanName);
    }

    @Override
    public int getUpdateWorkersCount() {
        return threadPool.size();
    }

    @Override
    public Date getLastFeedsUpdateRunDate() {
        return lastUpdateDate;
    }

    @Override
    public Date getLastFeedsUpdateInWorker(int workerNumber) {
        JBossSyncExecutor[] threads = threadPool.toArray(new JBossSyncExecutor[0]);
        return threads[workerNumber + 1].getLastFeedUpdateDate();
    }
}
