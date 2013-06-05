/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.presenter;

import com.google.gwt.event.shared.HandlerManager;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspaceViewPresenter extends MainViewPresenter {

    public interface Display extends MainViewPresenter.Display {
    }

    public WorkspaceViewPresenter(HandlerManager eventBus, Display view) {
        super(eventBus, view);
    }

    public void bind() {
        super.bind();
    }
}
