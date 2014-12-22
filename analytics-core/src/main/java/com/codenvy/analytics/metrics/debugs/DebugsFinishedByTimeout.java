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
package com.codenvy.analytics.metrics.debugs;

import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class DebugsFinishedByTimeout extends AbstractLongValueResulted {

    public DebugsFinishedByTimeout() {
        super(MetricType.DEBUGS_FINISHED_BY_TIMEOUT, TASK_ID);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.DEBUGS_FINISHED);
    }

    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(context);
        builder.put(MetricFilter.STOPPED_BY_USER, 0);
        return builder.build();
    }

    @Override
    public String getDescription() {
        return "The number of debugs stopped by timeout";
    }
}
