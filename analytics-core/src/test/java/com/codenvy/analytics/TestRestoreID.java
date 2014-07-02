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
package com.codenvy.analytics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.pig.udf.CreateProjectId;
import com.codenvy.analytics.pig.udf.ReplaceUserWithId;
import com.codenvy.analytics.pig.udf.ReplaceWsWithId;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.*;

/**
 * Modify analytics.properties:
 * analytics.mongodb.embedded=false
 * analytics.mongodb.url=mongodb://localhost:27017/organization
 *
 * @author Anatoliy Bazko
 */
public class TestRestoreID extends BaseTest {
    @BeforeClass
    @Override
    public void clearDatabase() {
    }

    @Test
    public void restore() throws Exception {
        for (String name : collectionsManagement.getNames()) {
            DBCollection collection = mongoDb.getCollection(name);

            LOG.info("Processing " + name);
            if (name.contains("precomputed") || name.contains("profiles")) {
                continue;
            }

            long total = collection.count();
            long num = 0;

            DBCursor dbCursor = collection.find();
            while (dbCursor.hasNext()) {
                boolean save = false;
                DBObject doc = dbCursor.next();

                String user = (String)doc.get(AbstractMetric.USER);
                if (user != null && !user.equals("default")) {
                    String userId = ReplaceUserWithId.exec(user);
                    if (!user.equals(userId)) {
                        save = true;
                        doc.put(AbstractMetric.USER, userId);
                    }
                }

                String ws = (String)doc.get(AbstractMetric.WS);
                if (ws != null && !ws.equals("default")) {
                    String wsId = ReplaceWsWithId.exec(ws);
                    if (!ws.equals(wsId)) {
                        save = true;
                        doc.put(AbstractMetric.WS, wsId);
                    }
                }

                String company = (String)doc.get(AbstractMetric.USER_COMPANY);
                if (company != null && user != null) {
                    String userId = ReplaceUserWithId.exec(user);

                    Context.Builder builder = new Context.Builder();
                    builder.put(MetricFilter.USER, userId);

                    Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
                    ListValueData list = getAsList(metric, builder.build());
                    if (list.size() != 1) {
                        if (!company.isEmpty()) {
                            save = true;
                            doc.put(AbstractMetric.USER_COMPANY, "");
                        }
                    } else {
                        Map<String, ValueData> profile = treatAsMap(treatAsList(list).get(0));
                        String newCompany = profile.get(AbstractMetric.USER_COMPANY).getAsString();
                        if (!company.equals(newCompany)) {
                            save = true;
                            doc.put(AbstractMetric.USER_COMPANY, newCompany);
                        }
                    }
                }

                String projectId = (String)doc.get(AbstractMetric.PROJECT_ID);
                if (projectId != null) {
                    String userId = ReplaceUserWithId.exec(user);
                    String wsId = ReplaceWsWithId.exec(ws);
                    String project = (String)doc.get(AbstractMetric.PROJECT);

                    String newProjectId = CreateProjectId.exec(userId, wsId, project);
                    if (!projectId.equals(newProjectId)) {
                        save = true;
                        doc.put(AbstractMetric.PROJECT_ID, newProjectId);
                    }
                }

                if (save) {
                    collection.save(doc);
                }

                num++;

                if (num % 100000 == 0) {
                    LOG.info((num * 100 / total) + "%");
                }
            }
        }
    }
}
