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
package com.codenvy.analytics.metrics.ide_usage;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public abstract class AbstractIdeUsage extends ReadBasedMetric implements ReadBasedExpandable {
    private final String[] sources;
    private String action;


    protected AbstractIdeUsage(String metricName, String... sources) {
        super(metricName);
        this.sources = sources;
    }

    protected AbstractIdeUsage(MetricType metricType, String... sources) {
        this(metricType.name(), sources);
    }

    protected AbstractIdeUsage(String action, String metricName, String... sources) {
        super(metricName);
        this.sources = sources;
        this.action = action;
    }

    protected AbstractIdeUsage(String action, MetricType metricType, String... sources) {
        this(action, metricType.name(), sources);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IDE_USAGES);
    }

    @Override
    public Context applySpecificFilter(Context clauses) {
        Context.Builder builder = new Context.Builder(clauses);
        builder.put(MetricFilter.SOURCE, sources);

        if (action != null) {
            builder.put(MetricFilter.ACTION, action);
        }

        return builder.build();
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
        DBObject group = new BasicDBObject();

        group.put(ID, null);
        group.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group)};
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }

    /**
     * @return simple name of action metric class without "Action" word and with words divided by space.
     * e.g. UploadFileAction simple metric class name => "Upload File"
     */
    @Override
    public String getDescription() {
        String description = getClass().getSimpleName();
        description = description.replaceAll("Action$", "");   // remove only last occurrence of "Action" word

        // divide separate words by space
        description = description.replaceAll("([A-Z][a-z]*)", "$1 "); // find out separate words and add space to them
        return description.trim();
    }

    @Override
    public String getExpandedField() {
        return PROJECT_ID;
    }
}
