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
package com.codenvy.analytics.metrics.user_event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 
 *
 * @author Alexander Reshetnyak
 */
public class UserEvent extends ReadBasedMetric {

    public static final String USER_EVENT_NAME = "user_event_name";
    public static final String COUNT = "count";

    public UserEvent() {
        super(MetricType.USER_EVENT);
    }
    
    @Override
    public String[] getTrackedFields() {
        return new String[]{USER_EVENT_NAME,
                             COUNT,
        };
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USER_EVENT);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        List<DBObject> dbOperations = new ArrayList<>();

        dbOperations.add(new BasicDBObject("$group",
                                           new BasicDBObject("_id", "$" + USER_EVENT_NAME)
                                                     .append(COUNT, new BasicDBObject("$sum", 1))
                                                     ));
        
        dbOperations.add(new BasicDBObject("$project",
                                           new BasicDBObject("_id", 0)
                                                   .append(USER_EVENT_NAME, "$_id")
                                                   .append(COUNT, 1)
                                                   ));
        
      return dbOperations.toArray(new DBObject[dbOperations.size()]);
    }
    
    @Override
    public String getDescription() {
        return "The user events count by user event type";
    }
}
