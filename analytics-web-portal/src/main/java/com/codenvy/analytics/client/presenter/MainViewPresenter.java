/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.presenter;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.event.AnalysisViewEvent;
import com.codenvy.analytics.client.event.ProjectViewEvent;
import com.codenvy.analytics.client.event.QueryViewEvent;
import com.codenvy.analytics.client.event.TimelineViewEvent;
import com.codenvy.analytics.client.event.UserViewEvent;
import com.codenvy.analytics.client.event.WorkspaceViewEvent;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class MainViewPresenter implements Presenter {
    private final HandlerManager eventBus;

    protected final Display      display;

    public interface Display {
        HasClickHandlers getTimelineViewButton();

        HasClickHandlers getWorkspaceViewButton();

        HasClickHandlers getUserViewButton();

        HasClickHandlers getProjectViewButton();

        HasClickHandlers getQueryViewButton();

        HasClickHandlers getAnalysisViewButton();

        HorizontalPanel getHeaderPanel();

        VerticalPanel getSubHeaderPanel();

        VerticalPanel getMainPanel();

        GWTLoader getGWTLoader();

        Widget asWidget();

        FlexTable getContentTable();

        /**
         * Show data onto page
         * 
         * @param data the retrieved data from the server
         */
        void setData(List<TableData> data);

        void setErrorMessage(String message);
    }

    public MainViewPresenter(HandlerManager eventBus, Display view) {
        this.eventBus = eventBus;
        this.display = view;
    }

    public void bind() {
        display.getTimelineViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new TimelineViewEvent());
            }
        });

        display.getWorkspaceViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new WorkspaceViewEvent());
            }
        });

        display.getUserViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new UserViewEvent());
            }
        });

        display.getProjectViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new ProjectViewEvent());
            }
        });

        display.getQueryViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new QueryViewEvent());
            }
        });

        display.getAnalysisViewButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new AnalysisViewEvent());
            }
        });
    }

    public void go(final HasWidgets container) {
        bind();

        container.clear();
        container.add(display.asWidget());
    }
}
