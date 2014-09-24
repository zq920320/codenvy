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
public class UploadFileAction extends AbstractIdeUsage {
    public static final String ACTION_ID = "IDE: Upload file";

    public UploadFileAction() {
        super(MetricType.UPLOAD_FILE_ACTION, ACTION_ID);
    }
}
