package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.MainViewPresenter;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class MainView extends Composite implements MainViewPresenter.Display {
    private final GWTLoader       gwtLoader     = new GWTLoader();

    private final Button          timelineViewButton;
    private final Button          workspaceViewButton;
    private final Button          userViewButton;

    private final Button          projectViewButton;
    private final Button          queryViewButton;

    private final VerticalPanel   mainPanel = new VerticalPanel();   ;
    private final HorizontalPanel headerPanel   = new HorizontalPanel();
    private final HorizontalPanel subHeaderPanel = new HorizontalPanel();

    public MainView() {
        timelineViewButton = new Button("Timeline");
        workspaceViewButton = new Button("Workspace");
        userViewButton = new Button("User");
        projectViewButton = new Button("Project");
        queryViewButton = new Button("Query");

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(timelineViewButton);
        hp.add(workspaceViewButton);
        hp.add(userViewButton);
        hp.add(projectViewButton);
        hp.add(queryViewButton);
        hp.getElement().setAttribute("align", "center");

        headerPanel.add(hp);
        headerPanel.setWidth("100%");
        headerPanel.getElement().setAttribute("align", "middle");

        subHeaderPanel.setWidth("100%");
        subHeaderPanel.getElement().setAttribute("align", "middle");

        mainPanel.add(headerPanel);
        mainPanel.add(subHeaderPanel);
        mainPanel.getElement().setAttribute("align", "center");

        initWidget(mainPanel);
    }

    public Widget asWidget() {
        return this;
    }

    public Button getTimelineViewButton() {
        return timelineViewButton;
    }


    public Button getWorkspaceViewButton() {
        return workspaceViewButton;
    }


    public Button getUserViewButton() {
        return userViewButton;
    }


    public Button getProjectViewButton() {
        return projectViewButton;
    }


    public Button getQueryViewButton() {
        return queryViewButton;
    }

    public VerticalPanel getMainPanel() {
        return mainPanel;
    }

    public HorizontalPanel getHeaderPanel() {
        return headerPanel;
    }

    public HorizontalPanel getSubHeaderPanel() {
        return subHeaderPanel;
    }

    public GWTLoader getGwtLoader() {
        return gwtLoader;
    }
}
