package com.codenvy.analytics.client.presenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class UserViewPresenter extends MainViewPresenter implements Presenter {
    public interface Display extends MainViewPresenter.Display {
        Label getLabel();

        Widget asWidget();
    }

    public UserViewPresenter(HandlerManager eventBus, Display view) {
        super(eventBus, view);
    }

    public void bind() {
        super.bind();
    }
}
