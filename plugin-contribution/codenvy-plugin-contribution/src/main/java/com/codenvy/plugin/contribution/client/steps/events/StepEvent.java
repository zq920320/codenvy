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

import com.google.gwt.event.shared.GwtEvent;

import javax.annotation.Nonnull;

/**
 * Event sent when a step is done or in error.
 *
 * @author Kevin Pollet
 */
public class StepEvent extends GwtEvent<StepHandler> {
    public static Type<StepHandler> TYPE = new Type<>();

    private final Step    step;
    private final boolean success;
    private final String  message;

    public StepEvent(@Nonnull final Step step, final boolean success) {
        this(step, success, null);
    }

    public StepEvent(@Nonnull final Step step, final boolean success, final String message) {
        this.step = step;
        this.success = success;
        this.message = message;
    }

    @Override
    public Type<StepHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(@Nonnull final StepHandler handler) {
        if (success) {
            handler.onStepDone(this);

        } else {
            handler.onStepError(this);
        }
    }

    public Step getStep() {
        return step;
    }

    public String getMessage() {
        return message;
    }

    public enum Step {
        COMMIT_WORKING_TREE,
        AUTHORIZE_CODENVY_ON_VCS_HOST,
        CREATE_FORK,
        CHECKOUT_BRANCH_TO_PUSH,
        ADD_FORK_REMOTE,
        PUSH_BRANCH_ON_FORK,
        ISSUE_PULL_REQUEST,
        GENERATE_REVIEW_FACTORY,
        ADD_REVIEW_FACTORY_LINK
    }
}
