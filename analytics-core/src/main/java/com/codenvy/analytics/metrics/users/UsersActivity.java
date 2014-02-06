/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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

import com.codenvy.analytics.metrics.AbstractCount;
import com.codenvy.analytics.metrics.MetricType;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersActivity extends AbstractCount {

    public UsersActivity() {
        super(MetricType.USERS_ACTIVITY, MetricType.USERS_ACTIVITY_LIST);
    }

    @Override
    public String getDescription() {
        return "The total number of users events";
    }
    
    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException, IOException {
        DBObject initialFilter = super.getFilter(clauses);
        DBObject match = (DBObject)initialFilter.get("$match");

        UsersActivityList.replaceFilterSessionIdWithUserAndDate(clauses, match);
        
        return initialFilter;
    }
}
