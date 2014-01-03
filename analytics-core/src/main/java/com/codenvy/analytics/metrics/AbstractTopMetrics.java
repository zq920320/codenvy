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

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ValueData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class AbstractTopMetrics extends ReadBasedMetric {
    private MetricType metricType;
    private String keyField;
    private int dayCount;
    
    private static final int LIFE_TIME_PERIOD = -1;
    private static final long DOCUMENT_COUNT = 100;
    
    public AbstractTopMetrics(MetricType metricType, int dayCount) {
        super(metricType);
        this.dayCount = dayCount;
    }

    @Override
    public Class< ? extends ValueData> getValueDataClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getTrackedFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject[] dbOperations = new DBObject[2];
        
        // sort
        boolean isAsc = true;        
        dbOperations[0] = new BasicDBObject("$sort", new BasicDBObject(keyField, isAsc ? 1 : -1));

        // get first 100 documents
        dbOperations[1] = new BasicDBObject("$limit", DOCUMENT_COUNT);

        return dbOperations;
    }

    
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        InitialValueContainer.validateExistenceInitialValueBefore(context);

        Parameters.TO_DATE.putDefaultValue(context);

        Calendar date = Calendar.getInstance();

        if (this.dayCount == LIFE_TIME_PERIOD) {
            Parameters.FROM_DATE.putDefaultValue(context);
        } else {
            try {
                date = Utils.getToDate(context);
            } catch (ParseException e) {
                throw new IllegalArgumentException("The illegal TO_DATE context parameter value '" + date);
            }

            date.add(Calendar.DAY_OF_MONTH, 1 - this.dayCount);   // starting from yesterday

            Utils.putFromDate(context, date);
        }
        
        return super.getValue(context);
    }
    
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(metricType);
    }
    
    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }
}
