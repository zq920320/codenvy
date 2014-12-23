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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import javax.annotation.security.RolesAllowed;

/** @author Dmytro Nochevnov */
@RolesAllowed(value = {"system/admin", "system/manager"})
public class TasksList extends AbstractListValueResulted implements ReadBasedSummariziable {
    public TasksList() {
        super(MetricType.TASKS_LIST);
    }

    @Override
    public String getDescription() {
        return "List of tasks";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.TASKS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            USER,
                            WS,
                            PROJECT,
                            PROJECT_TYPE,
                            PROJECT_ID,
                            PERSISTENT_WS,
                            TASK_ID,
                            TASK_TYPE,
                            MEMORY,
                            USAGE_TIME,
                            STARTED_TIME,
                            STOPPED_TME,
                            GIGABYTE_RAM_HOURS,
                            IS_FACTORY,
                            LAUNCH_TYPE,
                            SHUTDOWN_TYPE
        };
    }

    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(USAGE_TIME, new BasicDBObject("$sum", "$" + USAGE_TIME));
        group.put(GIGABYTE_RAM_HOURS, new BasicDBObject("$sum", "$" + GIGABYTE_RAM_HOURS));

        DBObject project = new BasicDBObject();
        project.put(USAGE_TIME, "$" + USAGE_TIME);
        project.put(GIGABYTE_RAM_HOURS, Utils.getRoundOperation(GIGABYTE_RAM_HOURS, 4));  // round GIGABYTE_RAM_HOURS to 0.0001

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", project)};
    }
}
