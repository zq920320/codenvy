/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps.events;

import com.codenvy.plugin.contribution.client.steps.Context;
import com.google.gwt.event.shared.GwtEvent;

import javax.annotation.Nonnull;

/**
 * @author Kevin Pollet
 */
public class ContextPropertyChangeEvent extends GwtEvent<ContextPropertyChangeHandler> {
    public static Type<ContextPropertyChangeHandler> TYPE = new Type<>();

    private final Context         context;
    private final ContextProperty contextProperty;

    public ContextPropertyChangeEvent(@Nonnull final Context context, @Nonnull final ContextProperty contextProperty) {
        this.context = context;
        this.contextProperty = contextProperty;
    }

    /**
     * Returns the context object.
     *
     * @return the context object.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the property changed.
     *
     * @return the property changed.
     */
    public ContextProperty getContextProperty() {
        return contextProperty;
    }

    @Override
    public Type<ContextPropertyChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final ContextPropertyChangeHandler handler) {
        handler.onContextPropertyChange(this);
    }

    public enum ContextProperty {
        PROJECT,
        ORIGIN_REPOSITORY_OWNER,
        ORIGIN_REPOSITORY_NAME,
        CLONED_BRANCH_NAME,
        WORK_BRANCH_NAME
    }
}
