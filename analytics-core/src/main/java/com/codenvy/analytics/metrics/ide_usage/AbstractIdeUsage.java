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
package com.codenvy.analytics.metrics.ide_usage;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public abstract class AbstractIdeUsage extends ReadBasedMetric implements ReadBasedExpandable {

    /* ide actions which is not has metric */
    public static final String FILE_NEW_FILE                 = "IDE: New file";
    public static final String PROJECT_CREATE_NEW_PROJECT    = "IDE: New project";
    public static final String PROJECT_IMPORT_FROM_GITHUB    = "IDE: Import project from GitHub";
    public static final String PROJECT_UPDATE_DEPENDENCIES   = "IDE: Update project dependencies";
    public static final String PROJECT_BUILD_WITH_PARAMETERS = "IDE: Build project with Maven parameter";
    public static final String RUN_DEBUG_APPLICATION         = "IDE: Debug application";
    public static final String HELP_TUTORIAL                 = "IDE: Show tutorial";

    private final String[] actions;

    protected AbstractIdeUsage(String metricName, String... actions) {
        super(metricName);
        this.actions = actions;
    }

    protected AbstractIdeUsage(MetricType metricType, String... types) {
        this(metricType.name(), types);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IDE_USAGES);
    }

    @Override
    public Context applySpecificFilter(Context clauses) {
        Context.Builder builder = new Context.Builder(clauses);
        builder.put(MetricFilter.ACTION, actions);
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
        description = description.replace("Action", "");

        // divide separate words by space
        description = description.replaceAll("([A-Z][a-z]*)", "$1 "); // find out separate words and add space to them
        if (description.endsWith(" ")) {   // remove redundant ended space
            description = description.substring(0, description.length() - 1);
        }

        return description;
    }

    @Override
    public String getExpandedField() {
        return PROJECT_ID;
    }
}
