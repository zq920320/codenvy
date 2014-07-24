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
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class FactoryStatisticsList extends AbstractListValueResulted implements ReadBasedSummariziable {

    public FactoryStatisticsList() {
        super(MetricType.FACTORY_STATISTICS_LIST);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS);
    }

    @Override
    public String getDescription() {
        return "The statistic of factory";
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FACTORY,
                            TIME,
                            RUNS,
                            DEPLOYS,
                            BUILDS,
                            DEBUGS,
                            SESSIONS,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION,
                            WS_CREATED,
                            ENCODED_FACTORY,
                            ORG_ID};
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject matchEmpty = new BasicDBObject();
        matchEmpty.put(FACTORY, new BasicDBObject("$ne", ""));

        DBObject matchNull = new BasicDBObject();
        matchNull.put(FACTORY, new BasicDBObject("$ne", null));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + FACTORY);
        group.put(ORG_ID, new BasicDBObject("$last", "$" + ORG_ID));
        group.put(TIME, new BasicDBObject("$sum", "$" + TIME));
        group.put(RUNS, new BasicDBObject("$sum", "$" + RUNS));
        group.put(DEPLOYS, new BasicDBObject("$sum", "$" + DEPLOYS));
        group.put(BUILDS, new BasicDBObject("$sum", "$" + BUILDS));
        group.put(DEBUGS, new BasicDBObject("$sum", "$" + DEBUGS));
        group.put(SESSIONS, new BasicDBObject("$sum", 1));
        group.put(AUTHENTICATED_SESSION, new BasicDBObject("$sum", "$" + REGISTERED_USER));
        group.put(CONVERTED_SESSION, new BasicDBObject("$sum", "$" + CONVERTED_SESSION));
        group.put(WS_CREATED, new BasicDBObject("$sum", "$" + WS_CREATED));
        group.put(ENCODED_FACTORY, new BasicDBObject("$sum", "$" + ENCODED_FACTORY));

        DBObject project = new BasicDBObject();
        project.put(FACTORY, "$_id");
        project.put(ORG_ID, 1);
        project.put(TIME, 1);
        project.put(RUNS, 1);
        project.put(DEPLOYS, 1);
        project.put(BUILDS, 1);
        project.put(DEBUGS, 1);
        project.put(SESSIONS, 1);
        project.put(AUTHENTICATED_SESSION, 1);
        project.put(CONVERTED_SESSION, 1);
        project.put(WS_CREATED, 1);
        project.put(ENCODED_FACTORY, 1);

        return new DBObject[]{new BasicDBObject("$match", matchEmpty),
                              new BasicDBObject("$match", matchNull),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject[] dbOperations = getSpecificDBOperations(clauses);
        ((DBObject)(dbOperations[2].get("$group"))).put(ID, null);
        ((DBObject)(dbOperations[3].get("$project"))).removeField(FACTORY);

        return dbOperations;
    }

    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        List<ValueData> list2Return = new ArrayList<>();

        for (ValueData row : ((ListValueData)valueData).getAll()) {
            MapValueData prevItems = (MapValueData)row;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            Map<String, ValueData> factoryData = getFactoryData(items2Return.get(FACTORY).getAsString());
            if (!factoryData.isEmpty()) {
                items2Return.put(USER, getNotDefaultStringValue(factoryData.get(USER)));
                items2Return.put(REPOSITORY, getNotNullStringValue(factoryData.get(REPOSITORY)));
                items2Return.put(PROJECT_TYPE, getNotNullStringValue(factoryData.get(PROJECT_TYPE)));
//                items2Return.put(ORG_ID, getNotNullStringValue(factoryData.get(ORG_ID)));
                items2Return.put(AFFILIATE_ID, getNotNullStringValue(factoryData.get(AFFILIATE_ID)));
            }

            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }

    private Map<String, ValueData> getFactoryData(String factory) throws IOException {
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
        return valueData == null || valueData.getAsString().equalsIgnoreCase("DEFAULT")
               ? StringValueData.DEFAULT
               : valueData;
    }
}
