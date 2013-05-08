package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.UserViewPresenter;
import com.google.gwt.user.client.ui.Label;

public class UserView extends MainView implements UserViewPresenter.Display {
    Label label = new Label("UserView");

    public UserView() {
        super();
        getMainPanel().add(label);
    }

    public Label getLabel() {
        return label;
    }

    public GWTLoader getGWTLoader() {
        return super.getGwtLoader();
    }
}
