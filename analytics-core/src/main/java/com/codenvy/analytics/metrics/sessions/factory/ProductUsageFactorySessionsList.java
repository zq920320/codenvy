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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class ProductUsageFactorySessionsList extends AbstractListValueResulted {
    public ProductUsageFactorySessionsList() {
        super(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{SESSION_ID,
                            USER,
                            WS,
                            DATE,
                            TIME,
                            REFERRER,
                            FACTORY,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        ReadBasedMetric metric = (ReadBasedMetric)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
        valueData = metric.postComputation(valueData, clauses);

        List<ValueData> list2Return = new ArrayList<>();
        for (ValueData items : ((ListValueData)valueData).getAll()) {
            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            long time = ValueDataUtil.treatAsLong(items2Return.get(TIME));
            long date = ValueDataUtil.treatAsLong(items2Return.get(DATE));

            items2Return.put(END_TIME, LongValueData.valueOf(time + date));

            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }

    @Override
    public String getDescription() {
        return "Factory sessions";
    }
}
