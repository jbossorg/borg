package org.jboss.planet.service;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.Cache;
import org.jboss.planet.event.MergePostsEvent;
import org.jboss.planet.exception.ParserException;
import org.jboss.planet.model.RemoteFeed;

/**
 * Executor takes delivered event and push it to Searchisko
 *
 * @author Libor Krzyzanek
 */
public class UpdateFeedsExecutor extends Thread {

    protected static final Logger log = Logger.getLogger(UpdateFeedsExecutor.class.getName());

    protected FeedsService feedsService;

    protected ParserService parserService;

    protected MergeService mergeService;

    protected Cache<Integer, Date> feedsToSync;

    protected Date lastFeedUpdateDate;

    private boolean running = false;

    private int intervalInSec;

    public UpdateFeedsExecutor(FeedsService feedsService, ParserService parserService, MergeService mergeService,
            Cache<Integer, Date> feedsToSync, int executorNumber, int updateIntervalInSec) {
        super("UpdateFeedsExecutor-" + executorNumber);
        this.feedsService = feedsService;
        this.parserService = parserService;
        this.mergeService = mergeService;
        this.feedsToSync = feedsToSync;
        this.intervalInSec = updateIntervalInSec;
    }

    @Override
    public void run() {
        log.log(Level.FINE, "Start Thread Execution.");
        running = true;

        while (running) {
            try {
                Thread.sleep(intervalInSec * 1000);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Interrupted. Quitting");
                running = false;
                break;
            }

            int newPosts = 0;
            int mergedPosts = 0;
            int totalPosts = 0;
            int ignoredPosts = 0;
            int duplicateTitles = 0;

            int counter = 0;
            int totalCountAtStart = feedsToSync.size();

            log.log(Level.FINE, "Start Update Feeds in thread. Total count of feeds to update: {0}", totalCountAtStart);

            Map.Entry<Integer, Date> entry;
            while ((entry = getEntryFromPool()) != null) {
                counter++;
                try {
                    RemoteFeed feed = feedsService.getFeed(entry.getKey());
                    boolean canContinue = feedsService.checkUpdateFails(feed);
                    if (!canContinue) {
                        if (log.isLoggable(Level.INFO)) {
                            log.log(Level.INFO,
                                    "Update Feed (" + counter + ", remaining: " + feedsToSync.size() + ") for '" + feed.getName() + "' is disabled.");
                        }
                        continue;
                    }

                    MergePostsEvent stat = syncFeed(feed);
                    lastFeedUpdateDate = entry.getValue();

                    newPosts += stat.getNewPosts();
                    mergedPosts += stat.getMergedPosts();
                    totalPosts += stat.getTotalPosts();
                    ignoredPosts += stat.getIgnoredPosts();
                    duplicateTitles += stat.getDuplicateTitles();
                } catch (ParserException pe) {
                    // already logged.
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Cannot update feed.", e);
                }
            }

            log.log(totalCountAtStart > 0 ? Level.INFO : Level.FINE,
                    "Update all feeds in thread finished. "
                            + "Total feeds: " + totalCountAtStart
                            + ", processed feeds: " + counter
                            + ", new posts: " + newPosts
                            + ", Merged posts: " + mergedPosts
                            + ", Ignored posts: " + ignoredPosts
                            + ", Duplicate titles: " + duplicateTitles
                            + ", Total posts: " + totalPosts);
        }
    }

    protected MergePostsEvent syncFeed(RemoteFeed feed) throws ParserException {
        try {
            RemoteFeed parsedFeed = parserService.parse(feed.getRemoteLink(), null, null);
            MergePostsEvent stat = mergeService.mergePosts(feed, parsedFeed.getPosts());

            if (log.isLoggable(Level.INFO)) {
                log.info("Update Feed for '" + feed.getName() + "' finished. "
                        + "STATS: new: " + stat.getNewPosts()
                        + ", merged: " + stat.getMergedPosts()
                        + ", ignored: " + stat.getIgnoredPosts()
                        + ", duplicate titles: " + stat.getDuplicateTitles()
                        + ", total: " + stat.getTotalPosts());
            }

            // reset update fail counter
            if (feed.getUpdateFailCount() != null && feed.getUpdateFailCount() > 0) {
                feed.setUpdateFailCount(0);
                feedsService.update(feed, false);
            }
            return stat;
        } catch (ParserException e) {
            String message = "Problem during parsing feed: "
                    + feed.getName() + ", cause type: " + e.getCauseType()
                    + ", parse fails: " + feed.getUpdateFailCount();
            if (ParserException.CAUSE_TYPE.CONNECTION_PROBLEM.equals(e.getCauseType())) {
                log.log(Level.SEVERE, message + ", Exception message: ", e.getMessage());
            } else {
                log.log(Level.SEVERE, message, e);
            }

            feed.incrementUpdateFailCount();
            feedsService.update(feed, false);

            throw e;
        }
    }

    protected Map.Entry<Integer, Date> getEntryFromPool() {
        // Synchronized to avoid java.util.NoSuchElementException in case that another thread is manipulating the pool
        synchronized (feedsToSync) {
            if (feedsToSync.size() > 0) {
                Map.Entry<Integer, Date> entry = feedsToSync.entrySet().iterator().next();
                feedsToSync.remove(entry.getKey());
                return entry;
            }
            return null;
        }
    }

    public void stopExecution() {
        running = false;
    }

}
