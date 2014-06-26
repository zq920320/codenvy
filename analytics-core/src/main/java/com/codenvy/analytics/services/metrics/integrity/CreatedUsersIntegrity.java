/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.services.metrics.integrity;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author Alexander Reshetnyak
 */
public class CreatedUsersIntegrity implements CollectionDataIntegrity {
    private static final Logger LOG = LoggerFactory.getLogger(CreatedUsersIntegrity.class);
    private final CollectionsManagement collectionsManagement;
    private static final int PAGE_SIZE = 10000;

    @Inject
    public CreatedUsersIntegrity(CollectionsManagement collectionsManagement) throws IOException {
        this.collectionsManagement = collectionsManagement;
    }

    @Override
    public void doCompute() throws IOException {
        // get users' profiles
        List<ValueData> usersProfiles = getUsersProfiles();

        // get users' events "user-created"
        DBCollection createdUsersCollection = collectionsManagement.getOrCreate(
                ((ReadBasedMetric)MetricFactory.getMetric(MetricType.CREATED_USERS)).getStorageCollectionName());
        Map<String, Long> usersCreatedEventsMap = getUsersWithoutUserCreatedEvent(createdUsersCollection);

        // calculate users without "user-created" events
        Map<String, DBObject> notExistUserCreatedEventMap =
                getUsersWithoutUserCreatedEvent(usersProfiles, usersCreatedEventsMap);

        // write new items in created_users collection
        write(createdUsersCollection, notExistUserCreatedEventMap.entrySet());
    }

    private List<ValueData> getUsersProfiles() throws IOException {
        List<ValueData> usersProfiles = new ArrayList<>();

        Metric usersProfilesList = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);

        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
        builder.put(Parameters.PER_PAGE, PAGE_SIZE);

        for (int pageNumber = 1; ; pageNumber++) {
            builder.put(Parameters.PAGE, pageNumber);

            ListValueData valueData = ValueDataUtil.getAsList(usersProfilesList, builder.build());
            usersProfiles.addAll(valueData.getAll());

            if (valueData.size() != PAGE_SIZE) {
                break;
            }
        }
        return usersProfiles;
    }

    private Map<String, Long> getUsersWithoutUserCreatedEvent(DBCollection createdUsersCollection) {
        Map<String, Long> usersCreatedEventsMap = new HashMap<>();

        for (int pageNumber = 1; ; pageNumber++) {
            AggregationOutput aggregation =
                    createdUsersCollection.aggregate(new BasicDBObject("$match", new BasicDBObject()),
                                                     getPaginationDBOperations(pageNumber,
                                                                               PAGE_SIZE));
            Iterator<DBObject> iterator = aggregation.results().iterator();

            long resultSize = 0;
            while (iterator.hasNext()) {
                DBObject record = iterator.next();
                if (usersCreatedEventsMap.containsKey(record.get(AbstractMetric.USER))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.warn("exists :" + record.get(AbstractMetric.USER) + ", "
                                 + usersCreatedEventsMap.get(record.get(AbstractMetric.USER)));
                        LOG.warn("new    :" + record.get(AbstractMetric.USER) + ", " + record.get(AbstractMetric.DATE));
                    }
                } else {
                    usersCreatedEventsMap
                            .put((String)record.get(AbstractMetric.USER), (Long)record.get(AbstractMetric.DATE));
                }
                resultSize++;
            }

            if (resultSize != PAGE_SIZE) {
                break;
            }
        }

        return usersCreatedEventsMap;
    }

    private Map<String, DBObject> getUsersWithoutUserCreatedEvent(List<ValueData> usersProfiles,
                                                                  Map<String, Long> usersCreatedMap) {
        Map<String, DBObject> notExistUserCreatedEventMap = new HashMap<>();

        DBCollection usersStatisticsCollection = collectionsManagement.getOrCreate(
                ((ReadBasedMetric)MetricFactory.getMetric(MetricType.USERS_STATISTICS_LIST))
                        .getStorageCollectionName());

        for (ValueData valueData : usersProfiles) {
            Map<String, ValueData> valueDataMap = ((MapValueData)valueData).getAll();
            String userId = valueDataMap.get(AbstractMetric.ID).getAsString();

            if (!usersCreatedMap.containsKey(userId)) {
                notExistUserCreatedEventMap
                        .put(userId, getFirstEvent(userId, usersStatisticsCollection));
            }
        }
        return notExistUserCreatedEventMap;
    }

    private DBObject getFirstEvent(String userId, DBCollection usersStatisticsCollection) {
        return usersStatisticsCollection.findOne(new BasicDBObject(AbstractMetric.USER, userId));
    }


    /** Provides basic DB pagination operations. */
    private DBObject[] getPaginationDBOperations(long page, long perPage) {
        DBObject[] dbOp = new DBObject[2];
        dbOp[0] = new BasicDBObject("$skip", (page - 1) * perPage);
        dbOp[1] = new BasicDBObject("$limit", perPage);
        return dbOp;
    }

    private void write(DBCollection dbCollection, Set<Map.Entry<String, DBObject>> items) throws IOException {
        long added = 0;
        for (Map.Entry<String, DBObject> item : items) {

            if (item.getValue() != null) {
                DBObject dbObject = new BasicDBObject();
                dbObject.put(AbstractMetric.ID, UUID.randomUUID().toString());
                dbObject.put(AbstractMetric.DATE, item.getValue().get(AbstractMetric.DATE));
                dbObject.put(AbstractMetric.PERSISTENT_WS, item.getValue().get(AbstractMetric.PERSISTENT_WS));
                dbObject.put(AbstractMetric.REGISTERED_USER, item.getValue().get(AbstractMetric.REGISTERED_USER));
                dbObject.put(AbstractMetric.USER, item.getKey());
                dbObject.put(AbstractMetric.VALUE, 1L);
                dbObject.put(AbstractMetric.WS, AbstractMetric.DEFAULT_VALUE);

                dbCollection.insert(dbObject);
                ++added;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.warn("Can not find any event by user : " + item.getKey());
                }
            }

        }
        LOG.info(added + " items were added in collection " + dbCollection.getName());
    }
}
