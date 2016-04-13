package org.jboss.planet.util;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * JMX Utils
 *
 * @author Libor Krzyzanek
 */
public class MBeanUtils {

    private static Logger log = Logger.getLogger(MBeanUtils.class.getName());

    /**
     * Helper to register a JMX Bean
     *
     * @param object
     * @param name
     * @return
     */
    public static ObjectName registerMBean(Object object, String name) {
        log.log(Level.INFO, "Register UpdateService MBean");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName mBeanName = new ObjectName(name);
            mbs.registerMBean(object, mBeanName);
            log.info("Registered MBean with name " + mBeanName.getCanonicalName());

            return mBeanName;
        } catch (MalformedObjectNameException e) {
            log.log(Level.SEVERE, "MBean name problem: " + e.getMessage(), e);
        } catch (InstanceAlreadyExistsException e) {
            log.log(Level.WARNING, "MBean registered before: " + e.getMessage());
        } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
            log.log(Level.SEVERE, "MBean registration failed: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Helper to unregister MBean
     *
     * @param mBeanName
     */
    public static void unregisterMBean(ObjectName mBeanName) {
        if (mBeanName != null) {
            log.log(Level.FINE, "going to unregister MBean");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean(mBeanName);
            } catch (MBeanRegistrationException e) {
                log.log(Level.WARNING, "MBean unregistration problem: " + e.getMessage(), e);
            } catch (InstanceNotFoundException e) {
                log.log(Level.SEVERE, "MBean was not registered: " + e.getMessage());
            }
        }

    }

}
