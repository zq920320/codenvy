/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.tasks.debugs;

import com.codenvy.analytics.metrics.AbstractSum;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class DebugsMemoryUsage extends AbstractSum {

    public DebugsMemoryUsage() {
        super(MetricType.DEBUGS_MEMORY_USAGE,
              MetricType.DEBUGS_FINISHED,
              MEMORY,
              TASK_ID);
    }

    @Override
    public String getDescription() {
        return "The memory usage in MB";
    }
}
