/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;


import com.codenvy.analytics.client.view.ProjectView;
import com.codenvy.analytics.client.view.QueryView;
import com.codenvy.analytics.client.view.TimeLineView;
import com.codenvy.analytics.client.view.UserView;
import com.codenvy.analytics.client.view.WorkspaceView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class AnalyticsApplication implements EntryPoint {
    private final QueryServiceAsync         queryService         = GWT.create(QueryService.class);
    private final TimeLineViewServiceAsync  viewService          = GWT.create(TimeLineViewService.class);
    private final QueryView                 queryView            = new QueryView(this);
    private final TimeLineView              timelineView         = new TimeLineView(this);
    private final UserView                  userView             = new UserView();
    private final WorkspaceView             workspaceView        = new WorkspaceView();
    private final ProjectView               projectView          = new ProjectView();

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

        RootPanel.get("mainMenuContainer").add(timelineButton);
        RootPanel.get("mainMenuContainer").add(userButton);
        RootPanel.get("mainMenuContainer").add(workspaceButton);
        RootPanel.get("mainMenuContainer").add(projectButton);
        RootPanel.get("mainMenuContainer").add(queryButton);

        // by default opening timeline view
        RootPanel.get("mainWindowContainer").add(timelineView);
    }

    public QueryServiceAsync getQueryService() {
        return queryService;
    }

    public TimeLineViewServiceAsync getViewService() {
        return viewService;
    }

    private class QueryClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(queryView);
        }
    }

    private class UserClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(userView);
        }
    }

    private class WorkspaceClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(workspaceView);
        }
    }

    private class ProjectClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(projectView);
        }
    }

    private class TimelineClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            RootPanel.get("mainWindowContainer").clear();
            RootPanel.get("mainWindowContainer").add(timelineView);
        }
    }
}
