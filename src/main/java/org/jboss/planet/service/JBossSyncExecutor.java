package org.jboss.planet.service;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.Cache;
import org.jboss.planet.model.PostStatus;

/**
 * @author Libor Krzyzanek
 */
public class JBossSyncExecutor extends Thread {
    protected static final Logger log = Logger.getLogger(JBossSyncExecutor.class.getName());

    protected JBossSyncService jbossSyncService;

    private boolean running = false;

    protected int intervalInSec;

    protected Cache<Integer, PostStatus> postsToSync;

    protected Date lastFeedUpdateDate;

    public JBossSyncExecutor(int executorNumber, JBossSyncService jbossSyncService, int intervalInSec, Cache<Integer, PostStatus> postsToSync) {
        super("SyncToDCPExecutor-" + executorNumber);
        this.jbossSyncService = jbossSyncService;
        this.intervalInSec = intervalInSec;
        this.postsToSync = postsToSync;
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

            int syncSucc = 0;
            int syncFail = 0;
            Map.Entry<Integer, PostStatus> entry;
            while ((entry = getEntryFromPool()) != null) {
                Integer id = entry.getKey();
                if (syncFail > 10 && syncSucc == 0) {
                    // Something is probably badly configured or DCP is down. Let's skip this update. Next happens very shortly
                    log.log(Level.SEVERE, "10 attempts sync to server failed. Aborting.");
                    break;
                }
                boolean success = jbossSyncService.syncPost(id, entry.getValue());
                if (success) {
                    syncSucc++;
                } else {
                    syncFail++;
                }
            }

            lastFeedUpdateDate = new Date();
            log.log((syncSucc + syncFail) != 0 ? Level.INFO : Level.FINE,
                    "Sync completed. Posts pushed to DCP: {0}, failed: {1}", new Integer[] { syncSucc, syncFail });
        }
    }

    protected Map.Entry<Integer, PostStatus> getEntryFromPool() {
        // Synchronized to avoid java.util.NoSuchElementException in case that another thread is manipulating the pool
        synchronized (postsToSync) {
            if (postsToSync.size() > 0) {
                Map.Entry<Integer, PostStatus> entry = postsToSync.entrySet().iterator().next();
                postsToSync.remove(entry.getKey());
                return entry;
            }
            return null;
        }
    }

    public Date getLastFeedUpdateDate() {
        return lastFeedUpdateDate;
    }

    public void stopExecution() {
        running = false;
    }

}