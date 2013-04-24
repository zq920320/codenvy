/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.codenvy.analytics.client.view.query.QueryViewImpl;

import com.codenvy.analytics.client.view.ProjectViewImpl;
import com.codenvy.analytics.client.view.TimeLineViewImpl;
import com.codenvy.analytics.client.view.UserViewImpl;
import com.codenvy.analytics.client.view.View;
import com.codenvy.analytics.client.view.WorkspaceViewImpl;
import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class AnalyticsApplication implements EntryPoint {
    private final QueryServiceAsync        queryService    = GWT.create(QueryService.class);
    private final TimeLineViewServiceAsync viewService     = GWT.create(TimeLineViewService.class);

    private final QueryViewImpl            queryView       = new QueryViewImpl(this);
    private final TimeLineViewImpl         timelineView    = new TimeLineViewImpl(this);
    private final UserViewImpl             userView        = new UserViewImpl();
    private final WorkspaceViewImpl        workspaceView   = new WorkspaceViewImpl();
    private final ProjectViewImpl          projectView     = new ProjectViewImpl();

    // values by default
    private View                           currentView     = timelineView;
    private TimeUnit                       currentTimeUnit = TimeUnit.DAY;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        final Button timelineButton = new Button("Timeline");
        timelineButton.setStyleName("mainmenu");
        timelineButton.addClickHandler(new TimelineClickHandler());

        final Button userButton = new Button("User");
        userButton.setStyleName("mainmenu");
        userButton.addClickHandler(new UserClickHandler());

        final Button workspaceButton = new Button("Workspace");
        workspaceButton.setStyleName("mainmenu");
        workspaceButton.addClickHandler(new WorkspaceClickHandler());

        final Button projectButton = new Button("Project");
        projectButton.setStyleName("mainmenu");
        projectButton.addClickHandler(new ProjectClickHandler());

        final Button queryButton = new Button("Query");
        queryButton.setStyleName("mainmenu");
        queryButton.addClickHandler(new QueryClickHandler());

        final ListBox timeUnitBox = new ListBox();
        initTimeUnitBox(timeUnitBox);

        RootPanel.get("mainMenuContainer").add(timelineButton);
        RootPanel.get("mainMenuContainer").add(userButton);
        RootPanel.get("mainMenuContainer").add(workspaceButton);
        RootPanel.get("mainMenuContainer").add(projectButton);
        RootPanel.get("mainMenuContainer").add(queryButton);
        RootPanel.get("timeUnitContainer").add(timeUnitBox);

        // by default opening timeline view
        RootPanel.get("mainWindowContainer").add((Widget)currentView);
    }

    private void initTimeUnitBox(final ListBox timeUnitBox) {
        for (TimeUnit timeUnit : TimeUnit.values()) {
            timeUnitBox.addItem(timeUnit.toString().toLowerCase());
        }

        timeUnitBox.setVisibleItemCount(1);
        timeUnitBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                currentTimeUnit = TimeUnit.values()[timeUnitBox.getSelectedIndex() - 1];
                currentView.update(currentTimeUnit);
            }
        });
    }

    public QueryServiceAsync getQueryService() {
        return queryService;
    }

    public TimeLineViewServiceAsync getViewService() {
        return viewService;
    }

    /**
     * @return {@link #currentTimeUnit}
     */
    public TimeUnit getCurrentTimeUnit() {
        return currentTimeUnit;
    }

    private class QueryClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(queryView);

            currentView = queryView;
        }
    }

    private class UserClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(userView);

            currentView = userView;
        }
    }

    private class WorkspaceClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(workspaceView);

            currentView = workspaceView;
        }
    }

    private class ProjectClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(projectView);

            currentView = projectView;
        }
    }

    private class TimelineClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(timelineView);

            currentView = timelineView;
        }
    }
}
