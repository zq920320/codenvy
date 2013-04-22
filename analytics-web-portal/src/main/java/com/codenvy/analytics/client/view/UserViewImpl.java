package com.codenvy.analytics.client.view;

import com.codenvy.analytics.shared.TimeUnit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class UserViewImpl extends Composite implements View {
    public UserViewImpl() {
        VerticalPanel vPanel = new VerticalPanel();
        Label label = new Label("User Test label");
        vPanel.add(label);
        initWidget(vPanel);
    }

    public void update(TimeUnit timeUnit) {
        // TODO Auto-generated method stub

    }
}
