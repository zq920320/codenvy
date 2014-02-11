/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractActiveEntities;
import com.codenvy.analytics.metrics.MetricType;

/** @author Dmytro Nochevnov */
public class UsersWhoCreatedProject extends AbstractActiveEntities {
    private static final String USER = "user";    
    
    public UsersWhoCreatedProject() {
        super(MetricType.USERS_WHO_CREATED_PROJECT, MetricType.CREATED_PROJECTS, USER);
    }
    
    @Override
    public String getDescription() {
        return "The number of users who created at least one project.";
    }
}
