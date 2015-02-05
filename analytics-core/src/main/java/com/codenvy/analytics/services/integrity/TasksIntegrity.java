/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

package com.codenvy.analytics.services.integrity;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

import static com.codenvy.analytics.metrics.AbstractMetric.DATE;
import static com.codenvy.analytics.metrics.AbstractMetric.ID;
import static com.codenvy.analytics.metrics.AbstractMetric.MEMORY;
import static com.codenvy.analytics.metrics.AbstractMetric.PERSISTENT_WS;
import static com.codenvy.analytics.metrics.AbstractMetric.PROJECT;
import static com.codenvy.analytics.metrics.AbstractMetric.PROJECT_ID;
import static com.codenvy.analytics.metrics.AbstractMetric.PROJECT_TYPE;
import static com.codenvy.analytics.metrics.AbstractMetric.REGISTERED_USER;
import static com.codenvy.analytics.metrics.AbstractMetric.STOP_TIME;
import static com.codenvy.analytics.metrics.AbstractMetric.TIMEOUT;
import static com.codenvy.analytics.metrics.AbstractMetric.USER;
import static com.codenvy.analytics.metrics.AbstractMetric.WS;
import static com.codenvy.analytics.pig.udf.CalculateGigabyteRamHours.calculateGigabyteRamHours;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class TasksIntegrity implements CollectionDataIntegrity {

    private final CollectionsManagement collectionsManagement;

    @Inject
    public TasksIntegrity(CollectionsManagement collectionsManagement) {
        this.collectionsManagement = collectionsManagement;
    }

    /** {@inheritDoc} */
    @Override
    public void doCompute(Context context) throws IOException {
        try {
            removeFinishedEventIfStartIsAbsent(context);
            updateUsageTime(context);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private void updateUsageTime(Context context) throws ParseException {
        removeObsoleteData(context);

        DBCollection collection = collectionsManagement.getOrCreate(MetricType.TASKS.toString().toLowerCase());

        DBObject q = Utils.setDateFilter(context, STOP_TIME);
        q.put(AbstractMetric.TASK_TYPE, new BasicDBObject("$in", new String[]{"builder", "runner", "debugger"}));
        q.put(AbstractMetric.START_TIME, new BasicDBObject("$exists", true));

        DBCursor cursor = collection.find(q);
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();

            String taskType = doc.get(AbstractMetric.TASK_TYPE).toString();
            String launchType = doc.get(AbstractMetric.LAUNCH_TYPE).toString();
            long startTime = Long.parseLong(doc.get(AbstractMetric.START_TIME).toString());
            long endTime = Long.parseLong(doc.get(AbstractMetric.STOP_TIME).toString());

            long timeout;
            if (doc.containsField(TIMEOUT)) {
                timeout = Long.parseLong(doc.get(TIMEOUT).toString());
            } else {
                if (taskType.equals("builder")) {
                    timeout = 300 * 1000;
                } else {
                    timeout = 3600 * 1000;
                }
            }

            long memory;
            if (doc.containsField(MEMORY)) {
                memory = Long.parseLong(doc.get(MEMORY).toString());
            } else {
                if (taskType.equals("builder")) {
                    memory = 1536;
                } else {
                    memory = 256;
                }
            }

            long usageTime = endTime - startTime;
            double gigabyteRamHours = calculateGigabyteRamHours(memory, usageTime);
            String shutDownType = launchType.equals("timeout") && usageTime > timeout ? "timeout"
                                                                                      : (taskType.equals("builder") ? "normal"
                                                                                                                    : "user");

            doc.put(AbstractMetric.USAGE_TIME, usageTime);
            doc.put(AbstractMetric.GIGABYTE_RAM_HOURS, gigabyteRamHours);
            doc.put(AbstractMetric.SHUTDOWN_TYPE, shutDownType);
            doc.put(TIMEOUT, timeout);
            doc.put(MEMORY, memory);

            collection.update(new BasicDBObject(ID, doc.get(ID)),
                              doc,
                              false,
                              false);

            Object date = doc.get(DATE);
            Object user = doc.get(USER);
            Object registeredUser = doc.get(REGISTERED_USER);
            Object persistentWs = doc.get(PERSISTENT_WS);
            Object ws = doc.get(WS);

            updateUsersStatisticsCollection(date, user, registeredUser, ws, persistentWs, usageTime, taskType);


            Object project = doc.get(PROJECT);
            Object projectType = doc.get(PROJECT_TYPE);
            Object projectId = doc.get(PROJECT_ID);
            updateProjectsStatisticsCollection(date, user, registeredUser, ws, persistentWs, usageTime, project, projectType, projectId,
                                               taskType);
        }
    }

    private void removeObsoleteData(Context context) throws ParseException {
        BasicDBObject[] docs = new BasicDBObject[3];
        docs[0] = new BasicDBObject("run_time", new BasicDBObject("$exists", true));
        docs[1] = new BasicDBObject("build_time", new BasicDBObject("$exists", true));
        docs[2] = new BasicDBObject("debug_time", new BasicDBObject("$exists", true));

        DBObject q = Utils.setDateFilter(context);
        q.put("$or", docs);

        DBCollection collection = collectionsManagement.getOrCreate(MetricType.USERS_STATISTICS.toString().toLowerCase());
        collection.remove(q);

        collection = collectionsManagement.getOrCreate(MetricType.PROJECTS_STATISTICS.toString().toLowerCase());
        collection.remove(q);
    }

    private void updateProjectsStatisticsCollection(Object date,
                                                    Object user,
                                                    Object registeredUser,
                                                    Object ws,
                                                    Object persistentWs,
                                                    long usageTime,
                                                    Object project,
                                                    Object projectType,
                                                    Object projectId,
                                                    String taskType) throws ParseException {
        DBCollection collection = collectionsManagement.getOrCreate(MetricType.PROJECTS_STATISTICS.toString().toLowerCase());
        doUpdateCollection(collection, date, user, registeredUser, ws, persistentWs, usageTime, project, projectType, projectId, taskType);
    }

    private void updateUsersStatisticsCollection(Object date,
                                                 Object user,
                                                 Object registeredUser,
                                                 Object ws,
                                                 Object persistentWs,
                                                 long usageTime,
                                                 String taskType) throws ParseException {
        DBCollection collection = collectionsManagement.getOrCreate(MetricType.USERS_STATISTICS.toString().toLowerCase());
        doUpdateCollection(collection, date, user, registeredUser, ws, persistentWs, usageTime, null, null, null, taskType);
    }

    private void doUpdateCollection(DBCollection collection,
                                    Object date,
                                    Object user,
                                    Object registeredUser,
                                    Object ws,
                                    Object persistentWs,
                                    long usageTime,
                                    @Nullable Object project,
                                    @Nullable Object projectType,
                                    @Nullable Object projectId,
                                    String taskType) throws ParseException {
        String field;
        switch (taskType) {
            case "debugger":
                field = "debug_time";
                break;
            case "runner":
                field = "run_time";
                break;
            case "builder":
                field = "build_time";
                break;
            default:
                throw new IllegalStateException("Task type is unknown " + taskType);
        }

        DBObject doc = new BasicDBObject();
        doc.put(ID, UUID.randomUUID().toString());
        doc.put(DATE, date);
        doc.put(USER, user);
        doc.put(REGISTERED_USER, registeredUser);
        doc.put(WS, ws);
        doc.put(PERSISTENT_WS, persistentWs);
        doc.put(field, usageTime);
        if (projectId != null) {
            doc.put(PROJECT, project);
            doc.put(PROJECT_TYPE, projectType);
            doc.put(PROJECT_ID, projectId);
        }
        collection.save(doc);
    }

    private void removeFinishedEventIfStartIsAbsent(Context context) throws ParseException {
        DBCollection collection = collectionsManagement.getOrCreate(MetricType.TASKS.toString().toLowerCase());

        DBObject q = Utils.setDateFilter(context, STOP_TIME);
        q.put(AbstractMetric.STOP_TIME, new BasicDBObject("$exists", true));
        q.put(AbstractMetric.START_TIME, new BasicDBObject("$exists", false));
        q.put(AbstractMetric.TASK_ID, new BasicDBObject("$exists", true));

        collection.remove(q);
    }
}
