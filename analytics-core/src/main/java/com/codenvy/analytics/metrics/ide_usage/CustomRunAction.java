/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.ide_usage;

import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/**
 * @author Dmytro Nochevnov
 */
@RolesAllowed({"user", "system/admin", "system/manager"})
public class CustomRunAction extends AbstractIdeUsage {
    /* TODO fix com.codenvy.ide.extension.runner.client.actions.CustomRunAction of IDE3
    to log "IDE: Custom run application" instead of "IDE: Run application" */
    public static final String ACTION_ID = "IDE: Custom run application";

    public CustomRunAction() {
        super(MetricType.CUSTOM_RUN_ACTION, ACTION_ID);
    }
}
