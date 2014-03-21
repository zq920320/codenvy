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
    public ValueData postEvaluation(ValueData valueData, Context clauses) throws IOException {
        ListValueData items = (ListValueData)valueData;
        if (items.size() > 1) {
            return items;
        }

        List<ValueData> list2Return = new ArrayList<>();
        for (ValueData row : items.getAll()) {
            MapValueData prevItems = (MapValueData)row;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            Map<String, ValueData> factoryData = getFactoryData(items2Return);
            items2Return.put(DATE, getNotNullStringValue(factoryData.get(DATE)));
            items2Return.put(USER, getNotDefaultStringValue(factoryData.get(USER)));
            items2Return.put(WS, getNotDefaultStringValue(factoryData.get(WS)));
            items2Return.put(REPOSITORY, getNotNullStringValue(factoryData.get(REPOSITORY)));
            items2Return.put(PROJECT_TYPE, getNotNullStringValue(factoryData.get(PROJECT_TYPE)));
            items2Return.put(ORG_ID, getNotNullStringValue(factoryData.get(ORG_ID)));
            items2Return.put(AFFILIATE_ID, getNotNullStringValue(factoryData.get(AFFILIATE_ID)));

            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }

    private Map<String, ValueData> getFactoryData(Map<String, ValueData> items) throws IOException {
        String factory = items.get(FACTORY).getAsString();
        Metric metric = MetricFactory.getMetric(MetricType.CREATED_FACTORIES_LIST);

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.FACTORY, factory);
        Context context = builder.build();

        ListValueData value = (ListValueData)metric.getValue(context);

        if (value.size() == 1) {
            return ((MapValueData)value.getAll().get(0)).getAll();
        } else {
            return MapValueData.DEFAULT.getAll();
        }
    }

    private ValueData getNotNullStringValue(ValueData valueData) {
        return valueData == null ? StringValueData.DEFAULT : valueData;
    }

    private ValueData getNotDefaultStringValue(ValueData valueData) {
        return valueData == null || valueData.getAsString().equalsIgnoreCase("DEFAULT") ? StringValueData.DEFAULT
                                                                                        : valueData;
    }
}
