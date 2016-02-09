/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
