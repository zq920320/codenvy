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
package com.codenvy.analytics.metrics;

import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class NumberOfUsersInStatistics extends AbstractCount {

    public NumberOfUsersInStatistics() {
        super(MetricType.NUMBER_OF_USERS_IN_STATISTICS, MetricType.USERS_STATISTICS);
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws IOException, ParseException {
        return super.getFilter(clauses);
    }

    @Override
    public String getDescription() {
        return "The number of users in statistics";
    }
}
