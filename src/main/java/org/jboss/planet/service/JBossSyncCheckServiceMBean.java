package org.jboss.planet.service;

import java.util.Date;

/**
 * MBean interface for monitoring JBossSyncCheckService
 *
 * @author Libor Krzyzanek
 */
public interface JBossSyncCheckServiceMBean {

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
