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
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public abstract class AbstractTopMetrics extends ReadBasedMetric {
    private int dayCount;
    
    private static int LIFE_TIME_PERIOD = -1;

    public AbstractTopMetrics(MetricType metricType, int dayCount) {
        super(metricType);
        this.dayCount = dayCount;
    }

    @Override
    public Class< ? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public String[] getTrackedFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        initContext(context, this.dayCount);
        
        return super.getValue(context);
    }

    /**
     * Setup proper FROM_DATE = (yesterday - dayCount)
     * @param context
     * @param dayCount
     */
    private void initContext(Map<String, String> context, int countOfDays) {
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
    
            date.add(Calendar.DAY_OF_MONTH, 1 - countOfDays);   // starting from yesterday
    
            Utils.putFromDate(context, date);
        }
    }


}
