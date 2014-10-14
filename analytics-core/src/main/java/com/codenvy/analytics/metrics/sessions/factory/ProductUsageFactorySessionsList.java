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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class ProductUsageFactorySessionsList extends AbstractListValueResulted implements ReadBasedSummariziable {

    public ProductUsageFactorySessionsList() {
        super(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{SESSION_ID,
                            USER,
                            SESSIONS,
                            TIME,
                            WS,
                            DATE,
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
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(SESSIONS, new BasicDBObject("$sum", 1));
        group.put(AUTHENTICATED_SESSION, new BasicDBObject("$sum", "$" + REGISTERED_USER));
        group.put(CONVERTED_SESSION, new BasicDBObject("$sum", "$" + CONVERTED_SESSION));

        DBObject project = new BasicDBObject();
        project.put(TIME, 1);
        project.put(SESSIONS, 1);
        project.put(AUTHENTICATED_SESSION, 1);
        project.put(CONVERTED_SESSION, 1);

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public String getDescription() {
        return "Factory sessions";
    }
}
