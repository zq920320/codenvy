/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client;

import com.codenvy.analytics.client.event.AnalysisViewEvent;
import com.codenvy.analytics.client.event.AnalysisViewEventHandler;
import com.codenvy.analytics.client.event.ProjectViewEvent;
import com.codenvy.analytics.client.event.ProjectViewEventHandler;
import com.codenvy.analytics.client.event.QueryViewEvent;
import com.codenvy.analytics.client.event.QueryViewEventHandler;
import com.codenvy.analytics.client.event.TimelineViewEvent;
import com.codenvy.analytics.client.event.TimelineViewEventHandler;
import com.codenvy.analytics.client.event.UserViewEvent;
import com.codenvy.analytics.client.event.UserViewEventHandler;
import com.codenvy.analytics.client.event.WorkspaceViewEvent;
import com.codenvy.analytics.client.event.WorkspaceViewEventHandler;
import com.codenvy.analytics.client.presenter.AnalysisViewPresenter;
import com.codenvy.analytics.client.presenter.Presenter;
import com.codenvy.analytics.client.presenter.ProjectViewPresenter;
import com.codenvy.analytics.client.presenter.QueryViewPresenter;
import com.codenvy.analytics.client.presenter.TimeLineViewPresenter;
import com.codenvy.analytics.client.presenter.UserViewPresenter;
import com.codenvy.analytics.client.presenter.WorkspaceViewPresenter;
import com.codenvy.analytics.client.view.AnalysisView;
import com.codenvy.analytics.client.view.ProjectView;
import com.codenvy.analytics.client.view.QueryView;
import com.codenvy.analytics.client.view.TimeLineView;
import com.codenvy.analytics.client.view.UserView;
import com.codenvy.analytics.client.view.WorkspaceView;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AppController implements Presenter, ValueChangeHandler<String> {
    private static final String        ANALYSIS  = "analysis";
    private static final String        QUERY     = "query";
    private static final String        PROJECT   = "project";
    private static final String        WORKSPACE = "workspace";
    private static final String        USER      = "user";
    private static final String        TIMELINE  = "timeline";

    private final HandlerManager       eventBus;
    private final QueryServiceAsync    queryService;
    private final UserServiceAsync     userService;
    private final TimeLineServiceAsync timelineService;
    private final AnalysisServiceAsync analysisService;

    private HasWidgets                 container;

    public AppController(QueryServiceAsync queryService,
                         TimeLineServiceAsync timelineService,
                         AnalysisServiceAsync analysisService,
                         UserServiceAsync userService,
                         HandlerManager eventBus) {

        this.eventBus = eventBus;
        this.queryService = queryService;
        this.timelineService = timelineService;
        this.analysisService = analysisService;
        this.userService = userService;

        bind();
    }

    private void bind() {
        History.addValueChangeHandler(this);

        eventBus.addHandler(TimelineViewEvent.TYPE,
                            new TimelineViewEventHandler() {
                                public void onLoad(TimelineViewEvent event) {
                                    doShowTimelineView();
                                }
                            });

        eventBus.addHandler(UserViewEvent.TYPE,
                            new UserViewEventHandler() {
                                public void onLoad(UserViewEvent event) {
                                    doShowUserView();
                                }
                            });

        eventBus.addHandler(WorkspaceViewEvent.TYPE,
                            new WorkspaceViewEventHandler() {
                                public void onLoad(WorkspaceViewEvent event) {
                                    doShowWorkspaceView();
                                }
                            });

        eventBus.addHandler(ProjectViewEvent.TYPE,
                            new ProjectViewEventHandler() {
                                public void onLoad(ProjectViewEvent event) {
                                    doShowProjectView();
                                }
                            });

        eventBus.addHandler(QueryViewEvent.TYPE,
                            new QueryViewEventHandler() {
                                public void onLoad(QueryViewEvent event) {
                                    doShowQueryView();
                                }
                            });

        eventBus.addHandler(AnalysisViewEvent.TYPE,
                            new AnalysisViewEventHandler() {
                                public void onLoad(AnalysisViewEvent event) {
                                    doAnalysisView();
                                }
                            });
    }

    private void doShowTimelineView() {
        History.newItem(TIMELINE);
    }

    private void doShowUserView() {
        History.newItem(USER);
    }

    private void doShowWorkspaceView() {
        History.newItem(WORKSPACE);
    }

    private void doShowProjectView() {
        History.newItem(PROJECT);
    }

    private void doShowQueryView() {
        History.newItem(QUERY);
    }

    private void doAnalysisView() {
        History.newItem(ANALYSIS);
    }

    public void go(final HasWidgets container) {
        this.container = container;
        if ("".equals(History.getToken())) {
            History.newItem(TIMELINE);
        }
        else {
            History.fireCurrentHistoryState();
        }
    }

    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();

        if (token != null) {
            Presenter presenter = null;

            if (token.equals(TIMELINE)) {
                presenter = new TimeLineViewPresenter(timelineService, eventBus, new TimeLineView());
            } else if (token.equals(USER)) {
                presenter = new UserViewPresenter(userService, eventBus, new UserView());
            } else if (token.equals(WORKSPACE)) {
                presenter = new WorkspaceViewPresenter(eventBus, new WorkspaceView());
            } else if (token.equals(PROJECT)) {
                presenter = new ProjectViewPresenter(eventBus, new ProjectView());
            } else if (token.equals(QUERY)) {
                presenter = new QueryViewPresenter(queryService, eventBus, new QueryView());
            } else if (token.equals(ANALYSIS)) {
                presenter = new AnalysisViewPresenter(analysisService, eventBus, new AnalysisView());
            }

            if (presenter != null) {
                presenter.go(container);
            }
        }
    }
}
