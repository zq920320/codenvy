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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public abstract class AbstractProjectPaas extends ReadBasedMetric implements ReadBasedExpandable {
    public static final String GAE              = "gae";
    public static final String AWS              = "aws";
    public static final String AWS_BEANSTALK    = "aws:beanstalk";
    public static final String CLOUDFOUNDRY     = "cloudfoundry";
    public static final String TIER3_WEB_FABRIC = "tier3 web fabric";
    public static final String MANYMO           = "manymo";
    public static final String OPENSHIFT        = "openshift";
    public static final String HEROKU           = "heroku";
    public static final String APPFOG           = "appfog";
    public static final String CLOUDBEES        = "cloudbees";

    public static final String[] PAASES = {GAE, AWS, AWS_BEANSTALK, CLOUDFOUNDRY, TIER3_WEB_FABRIC, MANYMO, OPENSHIFT, HEROKU, APPFOG, CLOUDBEES};

    private final String[] types;

    protected AbstractProjectPaas(MetricType metricType, String[] types) {
        super(metricType);

        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].toLowerCase();
        }
        this.types = types;
    }

    protected AbstractProjectPaas(String metricName, String[] types) {
        super(metricName);

        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].toLowerCase();
        }
        this.types = types;
    }


    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PROJECT_PAASES);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(PROJECT_PAAS, new BasicDBObject("$in", types));

        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(PROJECT_PAAS, new BasicDBObject("$in", types));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public String getExpandedField() {
        return PROJECT_ID;
    }

}
