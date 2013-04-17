package com.codenvy.analytics.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class UserView extends Composite {
    public UserView() {
        VerticalPanel vPanel = new VerticalPanel();
        Label label = new Label("User Test label");
        vPanel.add(label);
        initWidget(vPanel);
    }
}
