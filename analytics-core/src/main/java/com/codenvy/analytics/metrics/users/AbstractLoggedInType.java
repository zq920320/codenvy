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

import java.util.ArrayList;
import java.util.List;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractLoggedInType extends ReadBasedMetric implements Expandable {

    private final String[] types;

    private String expandingField = USER;

    protected AbstractLoggedInType(String metricName, String[] types) {
        super(metricName);
        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].toLowerCase();
        }
        this.types = types;
    }

    protected AbstractLoggedInType(MetricType metricType, String[] types) {
        this(metricType.name(), types);
    }

    @Override
    public String[] getTrackedFields() {
        return types;
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_LOGGED_IN_TYPES);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        for (String type : types) {
            group.put(type, new BasicDBObject("$sum", "$" + type));
        }

        return new DBObject[]{new BasicDBObject("$group", group)};
    }
    
    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        BasicDBObject[] dbObjectsToOr = new BasicDBObject[types.length];
        for (String type: types) {
            dbObjectsToOr[dbObjectsToOr.length-1] = new BasicDBObject(type, new BasicDBObject("$exists", true));
        }
        DBObject match = Utils.getOrOperation(dbObjectsToOr);
        
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + expandingField);

        DBObject projection = new BasicDBObject(expandingField, "$_id");

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }
    
    @Override
    public String getExpandedValueField() {
        return expandingField;
    }
}
