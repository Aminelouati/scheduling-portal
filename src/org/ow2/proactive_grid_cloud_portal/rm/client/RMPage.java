/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.rm.client;

import org.ow2.proactive_grid_cloud_portal.common.client.AboutWindow;
import org.ow2.proactive_grid_cloud_portal.common.client.CredentialsWindow;
import org.ow2.proactive_grid_cloud_portal.common.client.Images;
import org.ow2.proactive_grid_cloud_portal.common.client.Listeners.LogListener;
import org.ow2.proactive_grid_cloud_portal.common.client.LogWindow;
import org.ow2.proactive_grid_cloud_portal.rm.shared.RMConfig;

import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;


/**
 * Page shown when an user is logged
 * 
 * 
 * 
 * 
 * @author mschnoor
 *
 */
public class RMPage implements LogListener {

    private RMController controller = null;
    /** parent of all widgets held by this page */
    private Canvas rootLayout = null;

    /** displays all logs received from the event dispatcher */
    private LogWindow logWindow = null;
    /** about this app */
    private AboutWindow aboutWindow = null;
    /** client settings */
    private SettingsWindow settingsWindow = null;
    /** create and download credentials files */
    private CredentialsWindow credentialsWindow = null;
    /** create node sources */
    private NSCreationWindow nsWindow = null;

    /** top pane : textual list of nodes */
    private TreeView treeView = null;
    /** bot left pane : selected node info */
    private InfoView infoView = null;
    /** bot left pane : selected node monitoring */
    private MonitoringView monitoringView = null;
    /** bot right pane : graphical list of nodes */
    private CompactView compactView = null;
    /** bot left pane : node count */
    private StatisticsView statsView = null;
    /** bot right : graphs */
    private RMStatsView rmStatsView = null;

    /** displayed when critical log events occur */
    private ToolStripButton errorButton = null;
    private long lastCriticalMessage = 0;

    RMPage(RMController controller) {
        this.controller = controller;
        this.controller.getEventDispatcher().addLogListener(this);

        buildAndShow();
    }

    private void buildAndShow() {
        VLayout rl = new VLayout();
        this.rootLayout = rl;
        rl.setWidth100();
        rl.setHeight100();
        rl.setBackgroundColor("#fafafa");

        this.aboutWindow = new AboutWindow();
        this.settingsWindow = new SettingsWindow(controller);
        this.credentialsWindow = new CredentialsWindow();

        final Canvas header = buildTools();

        Canvas topPane = buildTopPane();
        topPane.setWidth100();
        topPane.setHeight100();
        Canvas botPane = buildBotPane();
        botPane.setWidth100();
        botPane.setHeight100();

        SectionStackSection topSection = new SectionStackSection();
        topSection.setTitle("Nodes");
        topSection.setExpanded(true);
        topSection.setItems(topPane);

        ImgButton expandButton = new ImgButton();
        expandButton.setWidth(16);
        expandButton.setHeight(16);
        expandButton.setSrc(Images.instance.expand_16().getSafeUri().asString());
        expandButton.setTooltip("Expand all items");
        expandButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                RMPage.this.treeView.expandAll();
            }
        });
        expandButton.setShowFocusedIcon(false);
        expandButton.setShowRollOver(false);
        expandButton.setShowDown(false);
        ImgButton closeButton = new ImgButton();
        closeButton.setWidth(16);
        closeButton.setHeight(16);
        closeButton.setSrc(Images.instance.close_16().getSafeUri().asString());
        closeButton.setTooltip("Collapse all items");
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                RMPage.this.treeView.closeAll();
            }
        });
        closeButton.setShowFocusedIcon(false);
        closeButton.setShowRollOver(false);
        closeButton.setShowDown(false);

        topSection.setControls(expandButton, closeButton);

        SectionStackSection botSection = new SectionStackSection();
        botSection.setTitle("Details");
        botSection.setExpanded(true);
        botSection.setItems(botPane);

        SectionStack stack = new SectionStack();
        stack.setWidth100();
        stack.setHeight100();
        stack.setMargin(2);
        stack.setVisibilityMode(VisibilityMode.MULTIPLE);
        stack.setAnimateSections(true);
        stack.setOverflow(Overflow.HIDDEN);
        stack.setSections(topSection, botSection);

        rl.addMember(header);
        rl.addMember(stack);

        this.logWindow = new LogWindow(controller);

        this.rootLayout.draw();
    }

    private ToolStrip buildTools() {
        ToolStrip tools = new ToolStrip();
        tools.setHeight(34);
        tools.setWidth100();
        tools.setBackgroundImage("");
        tools.setBackgroundColor("#fafafa");
        tools.setBorder("0px");

        MenuItem settingsMenuItem = new MenuItem("Settings", Images.instance.settings_16().getSafeUri()
                .asString());
        settingsMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                RMPage.this.settingsWindow.show();
            }
        });

        MenuItem credMenuItem = new MenuItem("Create credentials", Images.instance.key_16().getSafeUri()
                .asString());
        credMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                RMPage.this.credentialsWindow.show();
            }
        });

        MenuItem logoutMenuItem = new MenuItem("Logout", Images.instance.exit_16().getSafeUri().asString());
        logoutMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                SC.confirm("Logout", "Are you sure you want to exit?", new BooleanCallback() {
                    public void execute(Boolean value) {
                        if (value) {
                            RMPage.this.controller.logout();
                        }
                    }
                });
            }
        });

        ToolStripMenuButton portalMenuButton = new ToolStripMenuButton("Portal");
        Menu portalMenu = new Menu();
        portalMenu.setItems(credMenuItem, settingsMenuItem, new MenuItemSeparator(), logoutMenuItem);
        portalMenuButton.setMenu(portalMenu);

        MenuItem logMenuItem = new MenuItem("Display logs", Images.instance.log_16().getSafeUri().asString());
        logMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                RMPage.this.logWindow.show();
                errorButton.hide();
            }
        });

        MenuItem aboutMenuItem = new MenuItem("About", Images.instance.about_16().getSafeUri().asString());
        aboutMenuItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
            public void onClick(MenuItemClickEvent event) {
                RMPage.this.aboutWindow.show();
            }
        });
        ToolStripMenuButton helpMenuButton = new ToolStripMenuButton("Help");
        Menu helpMenu = new Menu();
        helpMenu.setItems(logMenuItem, aboutMenuItem);
        helpMenuButton.setMenu(helpMenu);

        String login = this.controller.getModel().getLogin();
        if (login != null)
            login = " <b>" + login + "</b>";
        else
            login = "";

        ToolStripButton nsButton = new ToolStripButton("Add Nodes");
        nsButton.setIcon(RMImages.instance.nodesource_16().getSafeUri().asString());
        nsButton.setTooltip("Create and add a new Node Source");
        nsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (RMPage.this.nsWindow != null)
                    RMPage.this.nsWindow.destroy();
                RMPage.this.nsWindow = new NSCreationWindow(controller);
                RMPage.this.nsWindow.show();
            }
        });

        ToolStripButton logoutButton = new ToolStripButton("Logout" + login);
        logoutButton.setIcon(Images.instance.exit_16().getSafeUri().asString());
        logoutButton.setTooltip("Logout");
        logoutButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                SC.confirm("Logout", "Are you sure you want to exit?", new BooleanCallback() {
                    public void execute(Boolean value) {
                        if (value) {
                            RMPage.this.controller.logout();
                        }
                    }
                });
            }
        });

        errorButton = new ToolStripButton("<strong>Network error</strong>", Images.instance.net_error_16()
                .getSafeUri().asString());
        errorButton.setBackgroundColor("#ffbbbb");
        errorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RMPage.this.logWindow.show();
                errorButton.hide();
            }
        });
        errorButton.hide();

        tools.addMenuButton(portalMenuButton);
        tools.addMenuButton(helpMenuButton);
        tools.addSeparator();
        tools.addButton(nsButton);
        tools.addButton(logoutButton);
        tools.addButton(errorButton);
        tools.addFill();
        tools.addMember(new Img(RMImages.instance.logo_32().getSafeUri().asString(), 154, 32));

        return tools;
    }

    private Canvas buildTopPane() {
        HLayout hl = new HLayout();
        hl.setWidth100();
        hl.setHeight100();

        this.treeView = new TreeView(controller);
        this.compactView = new CompactView(controller);

        Canvas treeCanvas = this.treeView.build();
        treeCanvas.setWidth("50%");
        treeCanvas.setShowResizeBar(true);
        Canvas compactCanvas = this.compactView.build();
        compactCanvas.setWidth("50%");

        hl.addMember(treeCanvas);
        hl.addMember(compactCanvas);

        return hl;
    }

    private Canvas buildBotPane() {
        final HLayout hl = new HLayout();
        hl.setWidth100();
        hl.setHeight100();

        this.infoView = new InfoView(controller);
        this.statsView = new StatisticsView(controller);

        final Canvas infoCanvas = this.infoView.build();
        Canvas statsCanvas = this.statsView.build();

        Tab t1 = new Tab("Selection");
        t1.setPane(infoCanvas);
        Tab t2 = new Tab("Nodes");
        t2.setPane(statsCanvas);

        final TabSet leftTabs = new TabSet();
        leftTabs.setWidth("50%");
        leftTabs.setShowResizeBar(true);
        leftTabs.setTabs(t1, t2);

        hl.addMember(leftTabs);

        // in offline charts are not displayed
        VisualizationUtils.loadVisualizationApi(new Runnable() {
            @Override
            public void run() {
                Tab t3 = new Tab("Monitoring");
                monitoringView = new MonitoringView(controller);
                Canvas monitoringCanvas = monitoringView.build();
                t3.setPane(monitoringCanvas);
                leftTabs.addTab(t3);

                rmStatsView = new RMStatsView(controller);
                final Canvas rmStatsCanvas = rmStatsView.build();
                rmStatsCanvas.setWidth("50%");
                hl.addMember(rmStatsCanvas);
            }
        }, CoreChart.PACKAGE);

        return hl;
    }

    void destroy() {
        this.rootLayout.destroy();
        this.logWindow.destroy();
        this.credentialsWindow.destroy();
        this.settingsWindow.destroy();
        this.aboutWindow.destroy();
        if (this.nsWindow != null)
            this.nsWindow.destroy();
    }

    @Override
    public void logMessage(String message) {
        long dt = System.currentTimeMillis() - this.lastCriticalMessage;
        if (dt > RMConfig.get().getClientRefreshTime() * 4) {
            this.errorButton.hide();
        }
    }

    @Override
    public void logImportantMessage(String message) {
        long dt = System.currentTimeMillis() - this.lastCriticalMessage;
        if (dt > RMConfig.get().getClientRefreshTime() * 4) {
            this.errorButton.hide();
        }
    }

    @Override
    public void logCriticalMessage(String message) {
        this.lastCriticalMessage = System.currentTimeMillis();
        this.errorButton.show();
    }
}
