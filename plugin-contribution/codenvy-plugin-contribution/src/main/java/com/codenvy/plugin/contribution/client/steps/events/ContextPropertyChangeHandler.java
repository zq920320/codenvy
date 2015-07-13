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

/**
 * Handler to be advised when a property of the context object is changed.
 *
 * @author Kevin Pollet
 */
public interface ContextPropertyChangeHandler extends EventHandler {
    /**
     * Called when a property of the context object changed.
     *
     * @param event
     *         the {@link ContextPropertyChangeEvent} event.
     */
    void onContextPropertyChange(ContextPropertyChangeEvent event);
}
