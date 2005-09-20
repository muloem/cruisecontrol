package net.sourceforge.cruisecontrol.distributed.util;

import org.apache.log4j.Logger;
import net.jini.core.lookup.ServiceItem;
import net.sourceforge.cruisecontrol.distributed.BuildAgentService;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.BadLocationException;
import javax.swing.SwingUtilities;
import java.rmi.RemoteException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: drollo
 * Date: Aug 1, 2005
 * Time: 4:00:38 PM
 * To change this template use File | Settings | File Templates.
 */
public final class BuildAgentUtility {
    private static final Logger LOG = Logger.getLogger(BuildAgentUtility.class);

    // @todo make BuidAgentService implement/extend jini ServiceUI?
    private static final class UI extends JFrame {
        private static final int CONSOLE_LINE_BUFFER_SIZE = 1000;

        private final BuildAgentUtility buildAgentUtility;
        private final JPanel northPanel;
        private final JButton btnRefresh = new JButton("Refresh");
        private final JComboBox cmbAgents = new JComboBox();
        private final JButton btnInvoke = new JButton("Invoke");
        private static final String METH_RESTART = "restart";
        private static final String METH_KILL = "kill";
        private final JComboBox cmbRestartOrKill = new JComboBox(new String[] {METH_RESTART, METH_KILL});
        private final JCheckBox chkAfterBuildFinished = new JCheckBox("Wait for build to finish.", true);
        private final JButton btnInvokeOnAll = new JButton("Invoke on All");
        private final JPanel pnlEdit = new JPanel(new BorderLayout());
        private final JButton btnClose = new JButton("Close");
        private final JTextArea txaConsole = new JTextArea();
        private final JScrollPane scrConsole = new JScrollPane();

        private UI(final BuildAgentUtility buildAgentUtil) {
            super("CruiseControl Distributed - Build Agent Utility");

            buildAgentUtility = buildAgentUtil;

            btnClose.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    exitForm();
                }
            });
            addWindowListener(new WindowAdapter() {
                public void windowClosing(final WindowEvent evt) {
                    exitForm();
                }
            });

            btnRefresh.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    refreshAgentList();
                }
            });

            cmbAgents.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    btnInvoke.setEnabled(true);
                }
            });

            btnInvoke.setEnabled(false);
            btnInvoke.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        invokeOnAgent(
                                ((ComboItemWrapper) cmbAgents.getSelectedItem()).getAgent()
                        );
                    } catch (RemoteException e1) {
                        appendInfo(e1.getMessage());
                        throw new RuntimeException(e1);
                    }
                }
            });

            btnInvokeOnAll.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    for (int i = 0; i < cmbAgents.getItemCount(); i++) {
                        try {
                            invokeOnAgent(((ComboItemWrapper) cmbAgents.getItemAt(i)).getAgent());
                        } catch (RemoteException e1) {
                            appendInfo(e1.getMessage());
                            //throw new RuntimeException(e1); // allow remaining items to be invoked
                        }
                    }
                }
            });

            txaConsole.setFont(new Font("Courier New", 0, 12));

            scrConsole.setViewportView(txaConsole);
            scrConsole.setPreferredSize(new Dimension(525, 300));


            getContentPane().setLayout(new BorderLayout());
            final JPanel pnlNN = new JPanel(new BorderLayout());
            pnlNN.add(btnRefresh, BorderLayout.WEST);
            pnlNN.add(cmbAgents, BorderLayout.CENTER);
            pnlNN.add(btnInvoke, BorderLayout.EAST);

            final JPanel pnlNS = new JPanel(new BorderLayout());
            pnlNS.add(btnClose, BorderLayout.EAST);

            pnlEdit.add(cmbRestartOrKill, BorderLayout.WEST);
            pnlEdit.add(chkAfterBuildFinished, BorderLayout.CENTER);
            pnlEdit.add(btnInvokeOnAll, BorderLayout.EAST);

            northPanel = new JPanel(new BorderLayout());
            northPanel.add(pnlNN, BorderLayout.NORTH);
            northPanel.add(pnlEdit, BorderLayout.CENTER);
            northPanel.add(pnlNS, BorderLayout.SOUTH);
            getContentPane().add(northPanel, BorderLayout.NORTH);
            getContentPane().add(scrConsole, BorderLayout.CENTER);
            pack();
            setVisible(true);
        }

        private void invokeOnAgent(final BuildAgentService agent) throws RemoteException {
            if (METH_RESTART.equals(cmbRestartOrKill.getSelectedItem())) {
                agent.restart(chkAfterBuildFinished.isSelected());
            } else {
                agent.kill(chkAfterBuildFinished.isSelected());
            }
        }

        private static final class ComboItemWrapper {
            private static ComboItemWrapper[] wrapArray(final ServiceItem[] serviceItems) {
                final ComboItemWrapper[] result = new ComboItemWrapper[serviceItems.length];
                for (int i = 0; i < serviceItems.length; i++) {
                    result[i] = new ComboItemWrapper(serviceItems[i]);
                }
                return result;
            }

            private final ServiceItem serviceItem;
            private ComboItemWrapper(final ServiceItem serviceItemToWrap) {
                this.serviceItem = serviceItemToWrap;
            }
            public BuildAgentService getAgent() {
                return (BuildAgentService) serviceItem.service;
            }
            public String toString() {
                try {
                    return getAgent().getMachineName() + ": " + serviceItem.serviceID;
                } catch (RemoteException e) {
                    return "Error: " + e.getMessage();
                }
            }
        }

        private void refreshAgentList() {
            btnRefresh.setEnabled(false);
            btnInvoke.setEnabled(false);
            btnInvokeOnAll.setEnabled(false);
            cmbAgents.setEnabled(false);
            new Thread() {
                public void run() {
                    try {
                        final List tmpList = new ArrayList();
                        final String agentInfoAll = buildAgentUtility.getAgentInfoAll(tmpList);
                        final ServiceItem[] serviceItems = (ServiceItem[]) tmpList.toArray(new ServiceItem[]{});
                        final ComboBoxModel comboBoxModel = new DefaultComboBoxModel(
                                ComboItemWrapper.wrapArray(serviceItems));
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                cmbAgents.setModel(comboBoxModel);
                            }
                        });
                        setInfo(agentInfoAll);
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                btnRefresh.setEnabled(true);
                                btnInvokeOnAll.setEnabled(true);
                                cmbAgents.setEnabled(true);
                            }
                        });
                    }
                }
            } .start();
        }

        private static void exitForm() {
            System.exit(0);
        }

        private void setInfo(final String infoText) {
            LOG.debug(infoText);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    txaConsole.setText(infoText);
                }
            });
        }

        private void appendInfo(final String infoText) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    txaConsole.append(infoText + "\n");
                    if (txaConsole.getLineCount() > CONSOLE_LINE_BUFFER_SIZE) {
                        // remove old lines
                        try {
                            txaConsole.replaceRange("", 0,
                                    txaConsole.getLineEndOffset(
                                            txaConsole.getLineCount() - CONSOLE_LINE_BUFFER_SIZE
                                    ));
                        } catch (BadLocationException e) {
                            ; //ignore
                        }
                    }
                    // Make sure the last line is always visible
                    txaConsole.setCaretPosition(txaConsole.getDocument().getLength());
                }
            });

        }
    }

    private final UI ui;


    private BuildAgentUtility() {
        ui = new UI(this);
        ui.btnRefresh.doClick();
    }

    private String getAgentInfoAll(final List lstServiceItems) {
        final String waitMessage = "Waiting 5 seconds for registrars to report in...";
        ui.setInfo(waitMessage);
        LOG.info(waitMessage);

        final StringBuffer result = new StringBuffer();
        try {
            // Use new instance each time to avoid stale cached info
            final MulticastDiscovery discovery = new MulticastDiscovery(null);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                LOG.warn("Sleep interrupted", e1);
            }

            final ServiceItem[] serviceItems
                    = discovery.getLookupCache().lookup(MulticastDiscovery.FLTR_ANY, Integer.MAX_VALUE);
            // don't wait for the terminate
            new Thread() {
                public void run() {
                    discovery.terminate();
                }
            } .start();

            // clear and rebuild list
            for (int i = 0; i < lstServiceItems.size(); i++) {
                lstServiceItems.remove(i);
            }
            for (int i = 0; i < serviceItems.length; i++) {
                lstServiceItems.add(serviceItems[i]);
            }

            result.append("Found: " + serviceItems.length + " agents.\n");
            ServiceItem serviceItem;
            BuildAgentService agent;
            String agentInfo;
            for (int x = 0; x < serviceItems.length; x++) {
                serviceItem = serviceItems[x];
                agent = (BuildAgentService) serviceItem.service;
                agentInfo = "Build Agent: " + serviceItem.serviceID + "\n"
                        + agent.asString()
                        + MulticastDiscovery.toStringEntries(serviceItem.attributeSets)
                        + "\n";
                LOG.debug(agentInfo);
                result.append(agentInfo);
            }
        } catch (RemoteException e) {
            final String message = "Search failed due to an unexpected error";
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }

        return result.toString();
    }

    public static void main(final String[] args) {
        new BuildAgentUtility();
    }
}