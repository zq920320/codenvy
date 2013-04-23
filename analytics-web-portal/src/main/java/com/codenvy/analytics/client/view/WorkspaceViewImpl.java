package com.codenvy.analytics.client.view;

import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class WorkspaceViewImpl extends Composite implements View {
    public WorkspaceViewImpl() {
        VerticalPanel vPanel = new VerticalPanel();
        Label label = new Label("Workspace Test label");
        vPanel.add(label);
        initWidget(vPanel);
    }

    public void update(TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }
}
