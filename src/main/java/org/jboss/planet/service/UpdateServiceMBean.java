package org.jboss.planet.service;

import java.util.Date;

/**
 * MBean interface for monitoring Update Service
 *
 * @author Libor Krzyzanek
 */
public interface UpdateServiceMBean {

    /**
     * Get count of workers
     *
     * @return
     */
    int getUpdateWorkersCount();

    /**
     * Get last time where feeds where set to be updated
     *
     * @return
     */
    Date getLastFeedsUpdateRunDate();

    /**
     * Get last time where worker performed feed update
     *
     * @param workerNumber
     * @return
     */
    Date getLastFeedsUpdateInWorker(int workerNumber);

}
