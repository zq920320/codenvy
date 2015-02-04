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
package com.codenvy.analytics;

import com.codenvy.analytics.metrics.AbstractMetric;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * DASHB-534
 *
 * @author Anatoliy Bazko
 */
public class TestRestoreLastLoginDate extends BaseTest {

    @BeforeClass
    @Override
    public void clearDatabase() {
    }

    @Test
    public void restore() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        DBCollection activity = mongoDb.getCollection("users_activity");

        DBObject match = new BasicDBObject();
        match.put(AbstractMetric.REGISTERED_USER, 1);
        match.put(AbstractMetric.EVENT, "user-sso-logged-in");

        DBObject group = new BasicDBObject();
        group.put("_id", "$user");
        group.put("lastLoginDate", new BasicDBObject("$max", "$date"));

        AggregationOutput output = activity.aggregate(new BasicDBObject("$match", match),
                                                      new BasicDBObject("$group", group));

        try (BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"))) {
            Iterator<DBObject> iterator = output.results().iterator();
            while (iterator.hasNext()) {
                DBObject next = iterator.next();
                long lastLoginDate = (long)next.get("lastLoginDate");
                next.put("lastLoginDate", df.format(new Date(lastLoginDate)));

                out.write(next.toString());
                out.newLine();
            }
        }
    }
}
