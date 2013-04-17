package com.codenvy.analytics.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class WorkspaceView extends Composite {
    public WorkspaceView() {
        VerticalPanel vPanel = new VerticalPanel();
        Label label = new Label("Workspace Test label");
        vPanel.add(label);
        initWidget(vPanel);
    }
}
