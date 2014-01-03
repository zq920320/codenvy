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
public abstract class AbstractTopSessions extends AbstractTopMetrics {   
    private int dayCount;

    private static final String KEY_FIELD = ProductUsageFactorySessionsList.TIME;
    private static final MetricType METRIC_TYPE = MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST;
    private static final int LIFE_TIME_PERIOD = -1;
    private static final long MAX_DOCUMENT_COUNT = 100;
    
    public AbstractTopSessions(MetricType factoryMetricType, int dayCount) {
        super(factoryMetricType);
    }

    @Override
    protected DBObject[] getSpecificDBOperations(Map<String, String> clauses) {
        DBObject[] dbOperations = new DBObject[2];
        
        // sort     
        dbOperations[0] = new BasicDBObject("$sort", new BasicDBObject(KEY_FIELD, 1));

        // get first 100 documents
        dbOperations[1] = new BasicDBObject("$limit", MAX_DOCUMENT_COUNT);

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
        return getStorageCollectionName(METRIC_TYPE);
    }
    
}
