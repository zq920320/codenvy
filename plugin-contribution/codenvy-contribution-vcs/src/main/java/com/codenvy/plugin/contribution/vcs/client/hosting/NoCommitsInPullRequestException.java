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

/**
 * Exception raised when a pull request is created with no commits.
 *
 * @author Kevin Pollet
 */
public class NoCommitsInPullRequestException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.vcs.client.hosting.NoCommitsInPullRequestException}.
     *
     * @param headBranch
     *         the head branch name.
     * @param baseBranch
     *         the base branch name.
     */
    public NoCommitsInPullRequestException(@Nonnull final String headBranch, @Nonnull final String baseBranch) {
        super("No commits between " + baseBranch + " and " + headBranch);
    }
}
