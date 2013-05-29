/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.WorkspaceViewPresenter;
import com.google.gwt.user.client.ui.Label;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspaceView extends MainView implements WorkspaceViewPresenter.Display {
    Label label = new Label("WorkspaceView");

    public WorkspaceView() {
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
