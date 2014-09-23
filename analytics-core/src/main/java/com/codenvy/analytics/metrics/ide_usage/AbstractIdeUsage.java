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
    /* ide actions */
    public static final String FILE_NEW_FILE      = "IDE: New file";
    public static final String UPLOAD_FILE_ACTION = "IDE: Upload file";
    public static final String SAVE_ACTION        = "IDE: Save file";
    public static final String SAVE_ALL_ACTION    = "IDE: Save all";
    public static final String DELETE_ITEM_ACTION = "IDE: Delete file";
    public static final String RENAME_ITEM_ACTION = "IDE: File rename";
    public static final String FORMATTER_ACTION   = "IDE: Format file";

    public static final String PROJECT_CREATE_NEW_PROJECT          = "IDE: New project";
    public static final String NEW_PROJECT_WIZARD_ACTION           = "IDE: New project from wizard";
    public static final String PROJECT_IMPORT_FROM_GITHUB          = "IDE: Import project from GitHub";
    public static final String IMPORT_PROJECT_FROM_LOCATION_ACTION = "IDE: Import project";
    public static final String OPEN_PROJECT_ACTION                 = "IDE: Open project";
    public static final String CLOSE_PROJECT_ACTION                = "IDE: Close project";
    public static final String NAVIGATE_TO_FILE_ACTION             = "IDE: Navigate to file";

    public static final String PROJECT_UPDATE_DEPENDENCIES   = "IDE: Update project dependencies";
    public static final String BUILD_ACTION                  = "IDE: Build project";
    public static final String PROJECT_BUILD_WITH_PARAMETERS = "IDE: Build project with Maven parameter";

    public static final String RUN_DEBUG_APPLICATION = "IDE: Debug application";
    public static final String RUN_ACTION            = "IDE: Run application";

    /* TODO fix com.codenvy.ide.extension.runner.client.actions.CustomRunAction of IDE3
       to log "IDE: Custom run application" instead of "IDE: Run application" */
    public static final String CUSTOM_RUN_ACTION = "IDE: Custom run application";

    public static final String STOP_ACTION     = "IDE: Stop application";
    public static final String GET_LOGS_ACTION = "IDE: Show application logs";

    public static final String REDIRECT_TO_FEEDBACK_ACTION = "IDE: Open Feedback window";
    public static final String REDIRECT_TO_FORUMS_ACTION   = "IDE: Open Forums window";
    public static final String REDIRECT_TO_HELP_ACTION     = "IDE: Open Help window";

    public static final String SHOW_PREFERENCES_ACTION = "IDE: Show preferences";
    public static final String SHOW_ABOUT_ACTION       = "IDE: Show about application";
    public static final String HELP_TUTORIAL           = "IDE: Show tutorial";

    public static final String AUTOCOMPLETING = "Autocompleting";

    /* plugin-git actions */
    public static final String ADD_TO_INDEX_ACTION      = "IDE: Git add";
    public static final String RESET_FILES_ACTION       = "IDE: Git reset";
    public static final String RESET_TO_COMMIT_ACTION   = "IDE: Git reset to commit";
    public static final String REMOVE_FROM_INDEX_ACTION = "IDE: Git remove";
    public static final String COMMIT_ACTION            = "IDE: Git commit";
    public static final String SHOW_BRANCHES_ACTION     = "IDE: Git show branches";
    public static final String SHOW_MERGE_ACTION        = "IDE: Git merge";
    public static final String INIT_REPOSITORY_ACTION   = "IDE: Git initialize repository";
    public static final String DELETE_REPOSITORY_ACTION = "IDE: Git delete repository";
    public static final String HISTORY_ACTION           = "IDE: Git show history";
    public static final String SHOW_STATUS_ACTION       = "IDE: Git status";
    public static final String SHOW_GIT_URL_ACTION      = "IDE: Git show git url";
    public static final String PUSH_ACTION              = "IDE: Git push";
    public static final String FETCH_ACTION             = "IDE: Git fetch";
    public static final String PULL_ACTION              = "IDE: Git pull";
    public static final String SHOW_REMOTE_ACTION       = "IDE: Git show remotes";

    /* plugin-hosted actions */
    public static final String SHARE_FACTORY_ACTION = "IDE: Share factory";

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
