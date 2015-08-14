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

package com.codenvy.analytics.integration;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricType;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static org.testng.AssertJUnit.assertTrue;


/**
 * @author Anatoliy Bazko
 */
public class TestActions extends BaseTest {

        @Test(dataProvider = "data")
        public void testAction(MetricType metricType) throws Exception {
        ValueData valueData = getValue(metricType);
        long l = treatAsLong(valueData);

        assertTrue(l > 0);
    }

        @DataProvider(name = "data")
        public static Object[][] data() {
                return new Object[][]{
                {MetricType.PROJECT_CONFIGURATION_ACTION},
                {MetricType.CLOSE_PROJECT_ACTION},
                {MetricType.DELETE_ITEM_ACTION},
                {MetricType.EXPAND_EDITOR_ACTION},
                {MetricType.FIND_ACTION_ACTION},
                {MetricType.FORMATTER_ACTION},
                {MetricType.IMPORT_PROJECT_FROM_LOCATION_ACTION},
                {MetricType.NAVIGATE_TO_FILE_ACTION},
                {MetricType.NEW_PROJECT_ACTION},
                {MetricType.OPEN_PROJECT_ACTION},
                {MetricType.REDIRECT_TO_FEEDBACK_ACTION},
                {MetricType.REDIRECT_TO_FORUMS_ACTION},
                {MetricType.REDIRECT_TO_HELP_ACTION},
                {MetricType.REDO_ACTION},
                {MetricType.RENAME_ITEM_ACTION},
                {MetricType.SAVE_ACTION},
                {MetricType.SAVE_ALL_ACTION},
                {MetricType.SHOW_ABOUT_ACTION},
                {MetricType.SHOW_PREFERENCES_ACTION},
                {MetricType.UNDO_ACTION},
                {MetricType.UPLOAD_FILE_ACTION},
                {MetricType.NEW_CSS_FILE_ACTION},
                {MetricType.NEW_LESS_FILE_ACTION},
                {MetricType.NEW_HTML_FILE_ACTION},
                {MetricType.NEW_JAVA_SCRIPT_FILE_ACTION},
                {MetricType.BUILD_ACTION},
                {MetricType.CLEAR_BUILDER_CONSOLE_ACTION},
                {MetricType.CLEAR_RUNNER_CONSOLE_ACTION},
                {MetricType.RUN_ACTION},
                {MetricType.SORT_BY_STATUS_ACTION},
                {MetricType.DEFAULT_NEW_RESOURCE_ACTION},
                {MetricType.NEW_FILE_ACTION},
                {MetricType.NEW_FOLDER_ACTION},
                {MetricType.NEW_XML_FILE_ACTION},
                {MetricType.SHUTDOWN_ACTION},
                {MetricType.ADD_TO_INDEX_ACTION},
                {MetricType.COMMIT_ACTION},
                {MetricType.DELETE_REPOSITORY_ACTION},
                {MetricType.FETCH_ACTION},
                {MetricType.HISTORY_ACTION},
                {MetricType.INIT_REPOSITORY_ACTION},
                {MetricType.PULL_ACTION},
                {MetricType.PUSH_ACTION},
                {MetricType.REMOVE_FROM_INDEX_ACTION},
                {MetricType.RESET_FILES_ACTION},
                {MetricType.RESET_TO_COMMIT_ACTION},
                {MetricType.SHOW_BRANCHES_ACTION},
                {MetricType.SHOW_GIT_URL_ACTION},
                {MetricType.SHOW_MERGE_ACTION},
                {MetricType.SHOW_REMOTE_ACTION},
                {MetricType.SHOW_STATUS_ACTION},
                {MetricType.MANAGE_DATASOURCES_ACTION},
                {MetricType.NEW_DATASOURCE_WIZARD_ACTION},
                {MetricType.NEW_SQL_FILE_ACTION},
                {MetricType.BOWER_INSTALL_ACTION},
                {MetricType.EXPORT_CONFIG_ACTION},
                {MetricType.IMPORT_FROM_CONFIG_ACTION},
                {MetricType.PERMISSIONS_ACTION},
                {MetricType.CUSTOM_GRUNT_RUN_ACTION},
                {MetricType.NPM_INSTALL_ACTION},
                {MetricType.CUSTOM_BUILD_ACTION},
                {MetricType.DEBUG_ACTION},
                {MetricType.NEW_JAVA_SOURCE_FILE_ACTION},
                {MetricType.NEW_PACKAGE_ACTION},
                {MetricType.UPDATE_DEPENDENCY_ACTION},
                {MetricType.SHOW_HIDDEN_FILES_ACTION},
                {MetricType.OPEN_SELECTED_FILE_ACTION},
                {MetricType.FIND_REPLACE_ACTION},
                {MetricType.BROWSE_TARGET_FOLDER_ACTION},
                {MetricType.CREATE_SUPPORT_TICKET_ACTION},
                {MetricType.REDIRECT_TO_ENGINEER_CHAT_CHANNEL_ACTION},
                {MetricType.SUBSCRIPTION_INDICATOR_ACTION},
                {MetricType.REDIRECT_LINK_ACTION},
                {MetricType.QUEUE_TYPE_INDICATOR_ACTION},
                {MetricType.MEMORY_INDICATOR_ACTION},
                {MetricType.PERMISSIONS_INDICATOR_ACTION},
                {MetricType.OPEN_WELCOME_PAGE_ACTION},
                {MetricType.SHARE_ACTION},
                {MetricType.REDIRECT_TO_DASHBOARD_ACTION},
                {MetricType.CREATE_MAVEN_MODULE_ACTION},
                {MetricType.QUICK_DOCUMENTATION_ACTION},
                {MetricType.OPEN_DECLARATION_ACTION}};
    }
}
