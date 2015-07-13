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

import com.google.gwt.event.shared.EventHandler;

import javax.annotation.Nonnull;

/**
 * Handler for step event.
 *
 * @author Kevin Pollet
 */
public interface StepHandler extends EventHandler {
    /**
     * Called when a step is successfully done.
     *
     * @param event
     *         the step event.
     */
    void onStepDone(@Nonnull StepEvent event);

    /**
     * Called when a step is in error.
     *
     * @param event
     *         the step event.
     */
    void onStepError(@Nonnull StepEvent event);
}
