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
    public static final String FILE_NEW_FILE    = "IDE: New file";
    public static final String FILE_UPLOAD_FILE = "IDE: Upload file";
    public static final String FILE_SAVE        = "IDE: Save file";
    public static final String FILE_SAVE_AS     = "IDE: Save all";
    public static final String FILE_DELETE      = "IDE: Delete file";
    public static final String FILE_RENAME      = "IDE: File rename";
    public static final String FILE_FORMAT      = "IDE: Format file";

    public static final String PROJECT_CREATE_NEW_PROJECT        = "IDE: New project";
    public static final String PROJECT_CREATE_NEW_PROJECT_WIZARD = "IDE: New project from wizard";
    public static final String PROJECT_IMPORT_FROM_GITHUB        = "IDE: Import project from GitHub";
    public static final String PROJECT_IMPORT_FROM               = "IDE: Import project";
    public static final String PROJECT_OPEN                      = "IDE: Open project";
    public static final String PROJECT_CLOSE                     = "IDE: Close project";
    public static final String PROJECT_OPEN_RESOURCE             = "IDE: Navigate to file";

    public static final String PROJECT_UPDATE_DEPENDENCIES   = "IDE: Update project dependencies";
    public static final String PROJECT_BUILD                 = "IDE: Build project";
    public static final String PROJECT_BUILD_WITH_PARAMETERS = "IDE: Build project with Maven parameter";

    public static final String RUN_DEBUG_APPLICATION    = "IDE: Debug application";
    public static final String RUN_RUN_APPLICATION      = "IDE: Run application";
    public static final String RUN_STOP_APPLICATION     = "IDE: Stop application";
    public static final String RUN_GET_APPLICATION_LOGS = "IDE: Show application logs";

    public static final String GIT_ADD               = "IDE: Git add";
    public static final String GIT_RESET             = "IDE: Git reset";
    public static final String GIT_RESET_TO_COMMIT   = "IDE: Git reset to commit";
    public static final String GIT_REMOVE            = "IDE: Git remove";
    public static final String GIT_COMMIT            = "IDE: Git commit";
    public static final String GIT_BRANCHES          = "IDE: Git show branches";
    public static final String GIT_MERGE             = "IDE: Git merge";
    public static final String GIT_INIT_REPOSITORY   = "IDE: Git initialize repository";
    public static final String GIT_DELETE_REPOSITORY = "IDE: Git delete repository";
    public static final String GIT_SHOW_HISTORY      = "IDE: Git show history";
    public static final String GIT_STATUS            = "IDE: Git status";
    public static final String GIT_GIT_URL_READ_ONLY = "IDE: Git show git url";
    public static final String GIT_REMOTE_PUSH       = "IDE: Git push";
    public static final String GIT_REMOTE_FETCH      = "IDE: Git fetch";
    public static final String GIT_REMOTE_PULL       = "IDE: Git pull";
    public static final String GIT_REMOTE_REMOTES    = "IDE: Git show remotes";

    public static final String WINDOW_PREFERENCES = "IDE: Show preferences";
    public static final String HELP_ABOUT         = "IDE: Show about application";
    public static final String HELP_TUTORIAL      = "IDE: Show tutorial";

    public static final String AUTOCOMPLETING = "Autocompleting";

    private final String[] actions;

    protected AbstractIdeUsage(String metricName, String[] actions) {
        super(metricName);
        this.actions = actions;
    }

    protected AbstractIdeUsage(MetricType metricType, String[] types) {
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

    // TODO
    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + getExpandedField());

        DBObject projection = new BasicDBObject(getExpandedField(), "$_id");

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$project", projection)};
    }
}