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

import com.codenvy.analytics.Utils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractUsersProfile extends ReadBasedMetric {

    public static final String USER_EMAIL      = "_id";
    public static final String USER_FIRST_NAME = "user_first_name";
    public static final String USER_LAST_NAME  = "user_last_name";
    public static final String USER_COMPANY    = "user_company";
    public static final String USER_JOB        = "user_job";
    public static final String USER_PHONE      = "user_phone";

    public AbstractUsersProfile(MetricType metricType) {
        super(metricType);
    }

    @Override
    public DBObject getFilter(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");

            if (filter == MetricFilter.USER) {
                match.put(USER_EMAIL, new BasicDBObject("$in", values));

            } else if (filter == MetricFilter.USER_COMPANY) {
                Pattern company = Pattern.compile(filter.get(clauses).replace(",", "|"), Pattern.CASE_INSENSITIVE);
                match.put(USER_COMPANY, company);

            } else {
                match.put(filter.toString().toLowerCase(), new BasicDBObject("$in", values));
            }
        }

        return new BasicDBObject("$match", match);
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        return new DBObject[0];
    }
}
