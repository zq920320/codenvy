/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.presenter;

import com.google.gwt.user.client.ui.HasWidgets;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract interface Presenter {
    public abstract void go(final HasWidgets container);
}
