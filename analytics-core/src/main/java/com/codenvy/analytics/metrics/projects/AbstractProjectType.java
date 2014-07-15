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
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProjectType extends ReadBasedMetric implements ReadBasedExpandable {

    public static final String   JAR                 = "jar";
    public static final String   JSP                 = "servlet/jsp";
    public static final String   DJANGO              = "django";
    public static final String   WAR                 = "war";
    public static final String   JAVA                = "java";
    public static final String   JAVA_ENGINE         = "app engine java";
    public static final String   MMP1                = "maven multi-module";
    public static final String   MMP2                = "maven_multi_module";
    public static final String   MMP3                = "maven";
    public static final String   SPRING              = "spring";
    public static final String   NODE_JS             = "nodejs";
    public static final String   PHP                 = "php";
    public static final String   PYTHON              = "python";
    public static final String   PYTHON_ENGINE       = "app engine python";
    public static final String   ANDROID             = "android";
    public static final String   GOOGLE_MBS          = "google-mbs-client-android";
    public static final String   OTHER_NULL          = "null";
    public static final String   OTHER_DEFAULT       = "default";
    public static final String   OTHER_SERV          = "serv";
    public static final String   OTHER_UNDEFINED     = "undefined";
    public static final String   OTHER_UNKNOWN       = "unknown";
    public static final String   OTHER_NAMELESS      = "nameless";
    public static final String   OTHER_EXO           = "exo";
    public static final String   RUBY                = "ruby";
    public static final String   RAILS               = "rails";
    public static final String   JAVA_SCRIPT         = "javascript";
    public static final String   ANGULAR_JAVA_SCRIPT = "angularjs";
    public static final String   HTML                = "html";
    public static final String[] TYPES               =
            {JAR, JSP, DJANGO, WAR, JAVA, JAVA_ENGINE, MMP1, MMP2, MMP3, SPRING, NODE_JS, PHP, PYTHON, PYTHON_ENGINE,
             ANDROID, GOOGLE_MBS, OTHER_NULL, OTHER_DEFAULT, OTHER_SERV, OTHER_EXO,
             RUBY, RAILS, JAVA_SCRIPT, ANGULAR_JAVA_SCRIPT, HTML, OTHER_UNDEFINED,
             OTHER_NAMELESS, OTHER_UNKNOWN};
    private final String[] types;

    protected AbstractProjectType(String metricName, String[] types) {
        super(metricName);

        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].toLowerCase();
        }
        this.types = types;
    }

    protected AbstractProjectType(MetricType metricType, String[] types) {
        this(metricType.name(), types);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PROJECTS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(PROJECT_TYPE, new BasicDBObject("$in", types));

        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject match = new BasicDBObject(PROJECT_TYPE, new BasicDBObject("$in", types));

        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$match", match),
                              new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    @Override
    public String getExpandedField() {
        return PROJECT_ID;
    }
}
