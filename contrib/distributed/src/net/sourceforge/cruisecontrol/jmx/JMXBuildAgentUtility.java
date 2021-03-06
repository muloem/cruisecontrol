package net.sourceforge.cruisecontrol.jmx;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.builders.DistributedMasterBuilder;
import net.sourceforge.cruisecontrol.distributed.util.BuildAgentUtility;
import net.sourceforge.cruisecontrol.distributed.BuildAgentService;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceItem;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

/**
 * @author Dan Rollo
 * Date: Sep 25, 2008
 * Time: 12:06:08 AM
 */
public class JMXBuildAgentUtility implements JMXBuildAgentUtilityMBean {

    private static final Logger LOG = Logger.getLogger(JMXBuildAgentUtility.class);

    private static final BuildAgentUtility AGENT_UTIL_SINGLETON = BuildAgentUtility.createForJMX();

    private boolean isAfterBuildFinished = true;

    private final List<ServiceRegistrar> lstRegistrars = new ArrayList<ServiceRegistrar>();
    private final List<String> lusIds = new ArrayList<String>();

    private List<ServiceItem> lstServiceItems = new ArrayList<ServiceItem>();
    private List<String> agentServiceIds = new ArrayList<String>();
    private String agentInfoAll;
    private long lastRefreshTime;
    // @todo make refresh timeout configurable
    private final long refreshTimeout = (BuildAgentUtility.LUS_WAIT_SECONDS + 1) * 1000;
    private void tryRefreshAgentList() throws RemoteException {

        // don't refresh until 5 seconds have elapsed since last refresh
        if ((System.currentTimeMillis() - lastRefreshTime) > refreshTimeout) {
            doRefresh();
        } else {
            LOG.debug("Skipping JMX Agent Util refresh, using cached agent info. timeout(millis): " + refreshTimeout);
        }
    }

    private void doRefresh() throws RemoteException {
        LOG.debug("JMX Agent Util refreshing...");

        lstServiceItems = new ArrayList<ServiceItem>();
        agentInfoAll = AGENT_UTIL_SINGLETON.getAgentInfoAll(lstServiceItems);

        agentServiceIds = new ArrayList<String>();
        for (ServiceItem serviceItem : lstServiceItems) {
            agentServiceIds.add(
                    ((BuildAgentService) serviceItem.service).getMachineName()
                            + ": " + serviceItem.serviceID);
        }

        // build list of LUS's after the above call to AGENT_UTIL_SINGLETON.getAgentInfoAll() to have recent data
        final ServiceRegistrar[] registrars = AGENT_UTIL_SINGLETON.getValidRegistrars();
        lstRegistrars.clear();
        lstRegistrars.addAll(Arrays.asList(registrars));
        
        lusIds.clear();
        for (final ServiceRegistrar lus : registrars) {
            lusIds.add(lus.getLocator().getHost() + ": "
                    + lus.getServiceID().toString());
        }

        lastRefreshTime = System.currentTimeMillis();

        LOG.debug("JMX Agent Util refresh complete.");
    }

    public void refresh() throws RemoteException {
        doRefresh();
    }


    public int getLookupServiceCount() throws RemoteException {
        tryRefreshAgentList();
        return AGENT_UTIL_SINGLETON.getLastLUSCount();
    }

    public String[] getLUSServiceIds() throws RemoteException {
        tryRefreshAgentList();
        return lusIds.toArray(new String[lusIds.size()]);
    }

    public void destroyLUS(final String lusServiceId) throws RemoteException, CruiseControlException {

        final ServiceRegistrar lus = findLUSViaServiceId(lusServiceId);
        if (lus != null) {
            LOG.debug("JMXBuildAgentUtility : LUS to destroy: " + lus.toString());

            // @todo Find a better way to get jini.httpPort sys prop set in main CC vm from cruise.properties
            DistributedMasterBuilder.loadJiniHttpPortIfNeeded();

            try {
                AGENT_UTIL_SINGLETON.destroyLookupService(lus);
            } catch (RemoteException e) {
                LOG.error("Error killing LookupService via JMX", e);
                throw e;
            } catch (Exception e) {
                LOG.error("Error killing LookupService via JMX", e);
                throw new CruiseControlException(e);
            }
        }
    }


    public String getBuildAgents() throws RemoteException {
        tryRefreshAgentList();
        return agentInfoAll;
    }

    public String[] getBuildAgentServiceIds() throws RemoteException {
        tryRefreshAgentList();
        return agentServiceIds.toArray(new String[agentServiceIds.size()]);
    }

    public boolean isKillOrRestartAfterBuildFinished() { return isAfterBuildFinished; }
    public void setKillOrRestartAfterBuildFinished(final boolean afterBuildFinished) {
        isAfterBuildFinished = afterBuildFinished;
    }

    public void kill(final String agentServiceId) throws RemoteException, CruiseControlException {

        final BuildAgentService buildAgentService = findAgentViaServiceId(agentServiceId);
        if (buildAgentService != null) {
            try {
                buildAgentService.kill(isAfterBuildFinished);
            } catch (RemoteException e) {
                LOG.error("Error killing Agent via JMX", e);
                throw e;
            } catch (Exception e) {
                LOG.error("Error killing Agent via JMX", e);
                throw new CruiseControlException(e);
            }
        }
    }
    public void killAll() throws  CruiseControlException {

        for (ServiceItem serviceItem : lstServiceItems) {
            try {
                kill(serviceItem.serviceID.toString());
            } catch (RemoteException e) {
                LOG.error("Error killing Agent via JMX", e);
            }
        }
    }

    public void restart(final String agentServiceId) throws RemoteException, CruiseControlException {

        final BuildAgentService buildAgentService = findAgentViaServiceId(agentServiceId);
        if (buildAgentService != null) {
            try {
                buildAgentService.restart(isAfterBuildFinished);
            } catch (RemoteException e) {
                final String msg = BuildAgentUtility.checkRestartRequiresWebStart(e);
                if (msg != null) {
                    LOG.error(msg, e);
                } else {
                    LOG.error("Error restarting Agent via JMX", e);
                }
                throw e;
            } catch (Exception e) {
                LOG.error("Error killing Agent via JMX", e);
                throw new CruiseControlException(e);
            }
        }
    }
    public void restartAll() throws  CruiseControlException {
        for (ServiceItem serviceItem : lstServiceItems) {
            try {
                restart(serviceItem.serviceID.toString());
            } catch (RemoteException e) {
                final String msg = BuildAgentUtility.checkRestartRequiresWebStart(e);
                if (msg != null) {
                    LOG.error(msg, e);
                } else {
                    LOG.error("Error restarting Agent via JMX", e);
                }
            }
        }
    }


    static final String MSG_NULL_SERVICEID = "ServiceId must not be null";
    
    private static String validateServiceId(final String agentServiceId) {
        if (agentServiceId == null) {
            throw new IllegalArgumentException(MSG_NULL_SERVICEID);
        }
        return agentServiceId.trim(); // JMX page can add spaces to values sent
    }

    private BuildAgentService findAgentViaServiceId(final String serviceIdUnTrimmed) {
        final String serviceId = validateServiceId(serviceIdUnTrimmed);

        for (ServiceItem serviceItem : lstServiceItems) {
            if (serviceItem.serviceID.toString().equals(serviceId)) {
                return (BuildAgentService) serviceItem.service;
            }
        }

        LOG.error("JMXBuildAgentUtility : Could not find Agent via serviceID: " + serviceId);
        return null;
    }

    private ServiceRegistrar findLUSViaServiceId(final String serviceIdUnTrimmed) {
        final String serviceId = validateServiceId(serviceIdUnTrimmed);

        for (ServiceRegistrar lus : lstRegistrars) {
            if (lus.getServiceID().toString().equals(serviceId)) {
                return lus;
            }
        }

        LOG.error("JMXBuildAgentUtility : Could not find LookupService via serviceID: " + serviceId);
        return null;
    }
}