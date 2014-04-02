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
public class FactoryStatisticsList extends AbstractListValueResulted {
    public FactoryStatisticsList() {
        super(MetricType.FACTORY_STATISTICS_LIST);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String getDescription() {
        return "The statistic of factory";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY,
                            TIME,
                            RUN,
                            DEPLOY,
                            BUILD,
                            SESSIONS,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION,
                            WS_CREATED};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject();
        match.put(FACTORY, new BasicDBObject("$ne", ""));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + FACTORY);
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(RUN, new BasicDBObject("$sum", "$" + RUN));
        group.put(DEPLOY, new BasicDBObject("$sum", "$" + DEPLOY));
        group.put(BUILD, new BasicDBObject("$sum", "$" + BUILD));
        group.put(SESSIONS, new BasicDBObject("$sum", 1));
        group.put(AUTHENTICATED_SESSION, new BasicDBObject("$sum", "$" + AUTHENTICATED_SESSION));
        group.put(CONVERTED_SESSION, new BasicDBObject("$sum", "$" + CONVERTED_SESSION));
        group.put(WS_CREATED, new BasicDBObject("$sum", "$" + WS_CREATED));

        DBObject project = new BasicDBObject();
        project.put(FACTORY, "$_id");
        project.put(TIME, 1);
        project.put(RUN, 1);
        project.put(DEPLOY, 1);
        project.put(BUILD, 1);
        project.put(SESSIONS, 1);
        project.put(AUTHENTICATED_SESSION, 1);
        project.put(CONVERTED_SESSION, 1);
        project.put(WS_CREATED, 1);

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        ListValueData items = (ListValueData)valueData;
        if (!clauses.exists(MetricFilter.FACTORY)) {
            return items;

        } else {
            List<ValueData> list2Return = new ArrayList<>();

            MapValueData prevItems = items.size() == 0 ? getDefaultItems() : (MapValueData)items.getAll().get(0);
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            Map<String, ValueData> factoryData = getFactoryData(clauses);
            if (factoryData.size() != 0) {
                items2Return.put(FACTORY, StringValueData.valueOf(clauses.getAsString(MetricFilter.FACTORY)));
                items2Return.put(USER, getNotDefaultStringValue(factoryData.get(USER)));
                items2Return.put(REPOSITORY, getNotNullStringValue(factoryData.get(REPOSITORY)));
                items2Return.put(PROJECT_TYPE, getNotNullStringValue(factoryData.get(PROJECT_TYPE)));
                items2Return.put(ORG_ID, getNotNullStringValue(factoryData.get(ORG_ID)));
                items2Return.put(AFFILIATE_ID, getNotNullStringValue(factoryData.get(AFFILIATE_ID)));
            }

            list2Return.add(new MapValueData(items2Return));

            return new ListValueData(list2Return);
        }
    }

    private MapValueData getDefaultItems() {
        return new MapValueData(new HashMap<String, ValueData>() {{
            put(TIME, LongValueData.DEFAULT);
            put(RUN, LongValueData.DEFAULT);
            put(DEPLOY, LongValueData.DEFAULT);
            put(BUILD, LongValueData.DEFAULT);
            put(SESSIONS, LongValueData.DEFAULT);
            put(AUTHENTICATED_SESSION, LongValueData.DEFAULT);
            put(CONVERTED_SESSION, LongValueData.DEFAULT);
            put(WS_CREATED, LongValueData.DEFAULT);
        }});
    }

    private Map<String, ValueData> getFactoryData(Context clauses) throws IOException {
        String factory = clauses.getAsString(MetricFilter.FACTORY);
        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, factory);
        Context context = builder.build();

        ListValueData value = (ListValueData)metric.getValue(context);

        Map<String, ValueData> result = new HashMap<>();
        for (ValueData row : value.getAll()) {
            MapValueData items = (MapValueData)row;

            for (Map.Entry<String, ValueData> entry : items.getAll().entrySet()) {
                String key = entry.getKey();

                if (!result.containsKey(key)) {
                    result.put(key, entry.getValue());
                } else if (!result.get(key).equals(entry.getValue())) {
                    result.put(key, StringValueData.DEFAULT);
                }
            }
        }

        return result;
    }

    private ValueData getNotNullStringValue(ValueData valueData) {
        return valueData == null ? StringValueData.DEFAULT : valueData;
    }

    private ValueData getNotDefaultStringValue(ValueData valueData) {
        return valueData == null || valueData.getAsString().equalsIgnoreCase("DEFAULT") ? StringValueData.DEFAULT
                                                                                        : valueData;
    }
}
