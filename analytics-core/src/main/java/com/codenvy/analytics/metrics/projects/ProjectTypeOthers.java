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

package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class ProjectTypeOthers extends AbstractProjectType {

    public ProjectTypeOthers() {
        super(MetricType.PROJECT_TYPE_OTHERS, new String[]{OTHER_NULL,
                                                           OTHER_DEFAULT,
                                                           OTHER_SERV,
                                                           OTHER_EXO,
                                                           OTHER_UNDEFINED,
                                                           OTHER_NAMELESS,
                                                           OTHER_UNKNOWN});
    }

    @Override
    public String getDescription() {
        return "The number of undefined projects types";
    }
}
