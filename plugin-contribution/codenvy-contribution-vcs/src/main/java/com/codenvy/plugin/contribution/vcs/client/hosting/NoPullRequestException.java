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

import javax.annotation.Nonnull;

public class NoPullRequestException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.vcs.client.hosting.NoPullRequestException}.
     *
     * @param branchName
     *         the branch name.
     */
    public NoPullRequestException(@Nonnull final String branchName) {
        super("No Pull Request for branch " + branchName);
    }
}
