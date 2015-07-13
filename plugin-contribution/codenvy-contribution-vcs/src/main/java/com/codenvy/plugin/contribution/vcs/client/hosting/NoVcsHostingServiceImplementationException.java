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
package com.codenvy.plugin.contribution.vcs.client.hosting;

/**
 * Exception raised when there is no {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService} implementation for the
 * current project.
 *
 * @author Kevin Pollet
 */
public class NoVcsHostingServiceImplementationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService}.
     */
    public NoVcsHostingServiceImplementationException() {
        super("No implementation of the VcsHostingService for the current project");
    }
}
