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
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public abstract class AbstractProjectPaas extends ReadBasedMetric implements Expandable {

    private final String paas;

    private String expandingField = PROJECT_ID;

    protected AbstractProjectPaas(String metricName, String paas) {
        super(metricName);
        
        paas = paas.toLowerCase();
        this.paas = paas;
    }

    protected AbstractProjectPaas(MetricType metricType, String paas) {
        this(metricType.name(), paas);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[] {paas};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PROJECT_PAASES);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        group.put(paas, new BasicDBObject("$sum", "$" + paas));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        String countingField = paas;
        DBObject match = new BasicDBObject(countingField, new BasicDBObject("$exists", true));
        
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
