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
package com.codenvy.plugin.contribution.projecttype.shared;

/**
 * Shared constants for the contribution project type.
 *
 * @author Kevin Pollet
 */
public final class ContributionProjectTypeConstants {
    public static final String CONTRIBUTION_PROJECT_TYPE_ID = "contribution";

    public static final String CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME = "contribution";

    /** Flag used to trigger the 'automatic' contribution flow. */
    public static final String CONTRIBUTE_VARIABLE_NAME = "contribute";

    /** Contribution mode variable used to know if we are in 'contribute' or 'review' mode. */
    public static final String CONTRIBUTE_MODE_VARIABLE_NAME = "contribute_mode";

    /** Contribution mode variable used to know in which branch the contribution has to be pushed. */
    public static final String CONTRIBUTE_BRANCH_VARIABLE_NAME = "contribute_branch";

    /** Contribution mode variable used to know the pull request id reviewed. */
    public static final String PULL_REQUEST_ID_VARIABLE_NAME = "pull_request_id";

    /**
     * Disable instantiation.
     */
    private ContributionProjectTypeConstants() {
    }
}
