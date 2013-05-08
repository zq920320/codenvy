package com.codenvy.analytics.client.presenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Label;

public class ProjectViewPresenter extends MainViewPresenter {

    public interface Display extends MainViewPresenter.Display {
        Label getLabel();
    }

    public ProjectViewPresenter(HandlerManager eventBus, Display view) {
        super(eventBus, view);
    }

    public void bind() {
        super.bind();
    }
}
