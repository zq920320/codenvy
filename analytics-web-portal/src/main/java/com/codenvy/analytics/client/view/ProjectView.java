package com.codenvy.analytics.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class ProjectView extends Composite {
    public ProjectView() {
        VerticalPanel vPanel = new VerticalPanel();
        Label label = new Label("Project Test label");
        vPanel.add(label);
        initWidget(vPanel);
    }
}
