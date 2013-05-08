package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.ProjectViewPresenter;
import com.google.gwt.user.client.ui.Label;

public class ProjectView extends MainView implements ProjectViewPresenter.Display {
    Label label = new Label("ProjectView");

    public ProjectView() {
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
