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

import com.codenvy.analytics.metrics.AbstractMapValueResulted;
import com.codenvy.analytics.metrics.MetricType;

/**
 * The IDE usage types definition.
 *
 * @author Alexander Reshetnyak
 */
public class IdeUsageTypes extends AbstractMapValueResulted {

    public static final String FILE_NEW_FOLDER  = "file-new-folder";
    public static final String FILE_NEW_PACKAGE = "file-new-package";

    public static final String FILE_NEW_TEXT_FILE       = "file-new-text-file";
    public static final String FILE_NEW_XML_FILE        = "file-new-xml-file";
    public static final String FILE_NEW_JSF_FILE        = "file-new-jsf-file";
    public static final String FILE_NEW_HTML_FILE       = "file-new-html-file";
    public static final String FILE_NEW_CSS_FILE        = "file-new-css-file";
    public static final String FILE_NEW_JAVA_CLASS_FILE = "file-new-java-class-file";
    public static final String FILE_NEW_JSP_FILE        = "file-new-jsp-file";
    public static final String FILE_NEW_RUBY_FILE       = "file-new-ruby-file";
    public static final String FILE_NEW_PHP_FILE        = "file-new-php-file";
    public static final String FILE_NEW_PYTHON_FILE     = "file-new-python-file";
    public static final String FILE_NEW_YAML_FILE       = "file-new-yaml-file";

    public static final String FILE_UPLOAD_FILE            = "file-upload-file";
    public static final String FILE_UPLOAD_ZIPPED_FOLDER   = "file-upload-zipped-folder";
    public static final String FILE_OPEN_LOCAL_FILE        = "file-open-local-file";
    public static final String FILE_OPEN_FILE_BY_PATH      = "file-open-file-by-path";
    public static final String FILE_OPEN_BY_URL            = "file-open-by-url";
    public static final String FILE_DOWNLOAD               = "file-download";
    public static final String FILE_DOWNLOAD_ZIPPED_FOLDER = "file-download-zipped-folder";
    public static final String FILE_SAVE                   = "file-save";
    public static final String FILE_SAVE_AS                = "file-save-as";
    public static final String FILE_CLOSE                  = "file-close";
    public static final String FILE_OPEN                   = "file-open";
    public static final String FILE_DELETE                 = "file-delete";
    public static final String FILE_RENAME                 = "file-rename";
    public static final String FILE_SEARCH                 = "file-search";

    public static final String PROJECT_CREATE_NEW_PROJECT                          = "project-create-new-project";
    public static final String PROJECT_CREATE_NEW_MODULE                           = "project-create-new-module";
    public static final String PROJECT_IMPORT_FROM_GITHUB                          = "project-import-from-github";
    public static final String PROJECT_OPEN                                        = "project-open";
    public static final String PROJECT_CLOSE                                       = "project-close";
    public static final String PROJECT_PROPERTIES_VIEW                             = "project-properties-view";
    public static final String PROJECT_OPEN_RESOURCE                               = "project-open-resource";
    public static final String PROJECT_ENABLE_COLLABORATION_MODE                   =
            "project-enable-collaboration-mode";
    public static final String PROJECT_USER_JOINED_WORKSPACE_IN_COLLABORATION_MODE =
            "project-user-joined-workspace-in-collaboration-mode";
    public static final String PROJECT_USER_LEFT_WORKSPACE                         = "project-user-left-workspace";
    public static final String PROJECT_UPDATE_DEPENDENCIES                         = "project-update-dependencies";
    public static final String PROJECT_SHOW_SYNTAX_ERROR_HIGHLIGHTING              =
            "project-show-syntax-error-highlighting";
    public static final String PROJECT_DISABLE_SYNTAX_ERROR_HIGHLIGHTING           =
            "project-disable-syntax-error-highlighting";
    public static final String PROJECT_BUILD                                       = "project-build";
    public static final String PROJECT_BUILD_PUBLISH                               = "project-build-publish";

    public static final String EDIT_CUT_ITEM             = "edit-cut-item";
    public static final String EDIT_COPY_ITEM            = "edit-copy-item";
    public static final String EDIT_PASTE_ITEM           = "edit-paste-item";
    public static final String EDIT_UNDO_TYPE            = "edit-undo-type";
    public static final String EDIT_REDO_TYPE            = "edit-redo-type";
    public static final String EDIT_FORMAT               = "edit-format";
    public static final String EDIT_ADD_BLOCK_COMMENT    = "edit-add-block-comment";
    public static final String EDIT_REMOVE_BLOCK_COMMENT = "edit-remove-block-comment";
    public static final String EDIT_TOGGLE_COMMENT       = "edit-toggle-comment";
    public static final String EDIT_FIND_AND_REPLACE     = "edit-find-and-replace";
    public static final String EDIT_HIDE_LINE_NUMBERS    = "edit-hide-line-numbers";
    public static final String EDIT_DELETE_CURRENT_LINE  = "edit-delete-current-line";
    public static final String EDIT_GO_TO_LINE           = "edit-go-to-line";
    public static final String EDIT_DELETE               = "edit-delete";
    public static final String EDIT_SELECT_ALL           = "edit-select-all";
    public static final String EDIT_MOVE_LINE_UP         = "edit-move-line-up";
    public static final String EDIT_MOVE_LINE_DOWN       = "edit-move-line-down";

    public static final String EDIT_SOURCE_GENERATE_GETTES_AND_SETTERS       =
            "edit-source-generate-gettes-and-setters";
    public static final String EDIT_SOURCE_GENERATE_CONSTRUCTOR_USING_FIELDS =
            "edit-source-generate-constructor-using-fields";

    public static final String EDIT_REFACTOR_RENAME = "edit-refactor-rename";

    public static final String VIEW_PROPERTIES        = "view-properties";
    public static final String VIEW_GO_TO_FOLDER      = "view-go-to-folder";
    public static final String VIEW_PROGRESS          = "view-progress";
    public static final String VIEW_OUTPUT            = "view-output";
    public static final String VIEW_LOG               = "view-log";
    public static final String VIEW_SHOW_HIDDEN_FILES = "view-show-hidden-files";
    public static final String VIEW_COLLABORATION     = "view-collaboration";

    public static final String RUN_DEBUG_APPLICATION = "run-debug-application";
    public static final String RUN_RUN_APPLICATION   = "run-run-application";
    public static final String RUN_STOP_APPLICATION  = "run-stop-application";
    public static final String RUN_SHOW_LOGS         = "run-show-logs";

    public static final String GIT_ADD               = "git-add";
    public static final String GIT_RESET             = "git-reset";
    public static final String GIT_REMOVE            = "git-remove";
    public static final String GIT_COMMIT            = "git-commit";
    public static final String GIT_BRANCHES          = "git-branches";
    public static final String GIT_MERGE             = "git-merge";
    public static final String GIT_RESET_INDEX       = "git-reset-index";
    public static final String GIT_CLONE_REPOSITORY  = "git-clone-repository";
    public static final String GIT_DELETE_REPOSITORY = "git-delete-repository";
    public static final String GIT_SHOW_HISTORY      = "git-show-history";
    public static final String GIT_STATUS            = "git-status";
    public static final String GIT_GIT_URL_READ_ONLY = "git-git-url-read-only";

    public static final String GIT_REMOTE_PUSH    = "git-remote-push";
    public static final String GIT_REMOTE_FETCH   = "git-remote-fetch";
    public static final String GIT_REMOTE_PULL    = "git-remote-pull";
    public static final String GIT_REMOTE_REMOTES = "git-remote-remotes";

    public static final String PAAS_MANY_MO_ANDROID_DEPLOY = "paas-many-mo-android-deploy";

    public static final String PAAS_APP_FOG_CREATE_APPLICATION = "paas-app-fog-create-application";
    public static final String PAAS_APP_FOG_APPLICATIONS       = "paas-app-fog-applications";
    public static final String PAAS_APP_FOG_SWITCH_ACCOUNT     = "paas-app-fog-switch-account";

    public static final String PAAS_CLOUD_BEES_CREATE_APPLICATION = "paas-cloud-bees-create-application";
    public static final String PAAS_CLOUD_BEES_APPLICATIONS       = "paas-cloud-bees-applications";
    public static final String PAAS_CLOUD_BEES_SWITCH_ACCOUNT     = "paas-cloud-bees-switch-account";
    public static final String PAAS_CLOUD_BEES_CREATE_ACCOUNT     = "paas-cloud-bees-create-account";

    public static final String PAAS_ELASTIC_BEANSTALK_CREATE_APPLICATION = "paas-elastic-beanstalk-create-application";
    public static final String PAAS_ELASTIC_BEANSTALK_MANAGE_APPLICATION = "paas-elastic-beanstalk-manage-application";
    public static final String PAAS_ELASTIC_BEANSTALK_SWITCH_ACCOUNT     = "paas-elastic-beanstalk-switch-account";
    public static final String PAAS_ELASTIC_BEANSTALK_EC2_MANAGEMENT     = "paas-elastic-beanstalk-ec2-management";
    public static final String PAAS_ELASTIC_BEANSTALK_S3_MANAGEMENT      = "paas-elastic-beanstalk-s3-management";

    public static final String PAAS_GOOGLE_APP_ENGINE_UPDATE_APPLICATION = "paas-google-app-engine-update-application";
    public static final String PAAS_GOOGLE_APP_ENGINE_CREATE_APPLICATION = "paas-google-app-engine-create-application";
    public static final String PAAS_GOOGLE_APP_ENGINE_LOGIN              = "paas-google-app-engine-login";

    public static final String PAAS_HEROKU_CREATE_APPLICATION = "paas-google-app-engine-login";
    public static final String PAAS_HEROKU_APPLICATIONS       = "paas-heroku-applications";
    public static final String PAAS_HEROKU_DEPLOY_PUBLIC_KEY  = "paas-heroku-deploy-public-key";
    public static final String PAAS_HEROKU_SWITCH_ACCOUNT     = "paas-heroku-switch-account";

    public static final String PAAS_OPEN_SHIFT_CHANGE_NAMESPACE      = "paas-open-shift-change-namespace";
    public static final String PAAS_OPEN_SHIFT_APPLICATIONS          = "paas-open-shift-applications";
    public static final String PAAS_OPEN_SHIFT_UPDATE_SHH_PUBLIC_KEY = "paas-open-shift-update-shh-public-key";
    public static final String PAAS_OPEN_SHIFT_SWITCH_ACCOUNT        = "paas-open-shift-switch-account";

    public static final String WINDOW_SHOW_VIEW_PROJECT_EXPLORE = "window-show-view-project-explore";
    public static final String WINDOW_SHOW_VIEW_PACKAGE_EXPLORE = "window-show-view-package-explore";

    public static final String WINDOW_NAVIGATION_NEXT_EDITOR     = "window-navigation-next-editor";
    public static final String WINDOW_NAVIGATION_PREVIOUS_EDITOR = "window-navigation-previous-editor";
    public static final String WINDOW_WELCOME                    = "window-welcome";
    public static final String WINDOW_PREFERENCES                = "window-preferences";
    
    public static final String SHARE_FACTORY_URL                 = "share-factory-url";
    public static final String SHARE_MANAGE_ACCESS               = "share-manage-access";
    public static final String SHARE_INVITE_DEVELOPERS           = "share-invite-developers";
    public static final String SHARE_INVITE_GITHUB_COLLABORATOS  = "share-invite-github-collaboratos";

    public static final String HELP_ABOUT                        = "help-about";
    public static final String HELP_SHOW_KEYBOARD_SHORTCUTS      = "help-show-keyboard-shortcuts";
    public static final String HELP_DOCUMENTATION                = "help-documentation";
    public static final String HELP_CONTACT_SUPPORT              = "help-contact-support";
    public static final String HELP_SUBMIT_FEEDBACK              = "help-submit-feedback";

    public static final String SHELL   = "shell";
    public static final String PROFILE = "profile";
    public static final String LOGOUT  = "logout";

    public IdeUsageTypes(String metricName) {
        super(metricName);
    }

    public IdeUsageTypes() {
        super(MetricType.IDE_USAGES);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{FILE_NEW_FOLDER,
                            FILE_NEW_PACKAGE,
                            FILE_NEW_TEXT_FILE,
                            FILE_NEW_XML_FILE,
                            FILE_NEW_JSF_FILE,
                            FILE_NEW_HTML_FILE,
                            FILE_NEW_CSS_FILE,
                            FILE_NEW_JAVA_CLASS_FILE,
                            FILE_NEW_JSP_FILE,
                            FILE_NEW_RUBY_FILE,
                            FILE_NEW_PHP_FILE,
                            FILE_NEW_PYTHON_FILE,
                            FILE_NEW_YAML_FILE,

                            FILE_UPLOAD_FILE,
                            FILE_UPLOAD_ZIPPED_FOLDER,
                            FILE_OPEN_LOCAL_FILE,
                            FILE_OPEN_FILE_BY_PATH,
                            FILE_OPEN_BY_URL,
                            FILE_DOWNLOAD,
                            FILE_DOWNLOAD_ZIPPED_FOLDER,
                            FILE_SAVE,
                            FILE_SAVE_AS,
                            FILE_CLOSE,
                            FILE_OPEN,
                            FILE_DELETE,
                            FILE_RENAME,
                            FILE_SEARCH,

                            PROJECT_CREATE_NEW_PROJECT,
                            PROJECT_CREATE_NEW_MODULE,
                            PROJECT_IMPORT_FROM_GITHUB,
                            PROJECT_OPEN,
                            PROJECT_CLOSE,
                            PROJECT_PROPERTIES_VIEW,
                            PROJECT_OPEN_RESOURCE,
                            PROJECT_ENABLE_COLLABORATION_MODE,
                            PROJECT_USER_JOINED_WORKSPACE_IN_COLLABORATION_MODE,
                            PROJECT_USER_LEFT_WORKSPACE,
                            PROJECT_UPDATE_DEPENDENCIES,
                            PROJECT_SHOW_SYNTAX_ERROR_HIGHLIGHTING,
                            PROJECT_DISABLE_SYNTAX_ERROR_HIGHLIGHTING,
                            PROJECT_BUILD,
                            PROJECT_BUILD_PUBLISH,

                            EDIT_CUT_ITEM,
                            EDIT_COPY_ITEM,
                            EDIT_PASTE_ITEM,
                            EDIT_UNDO_TYPE,
                            EDIT_REDO_TYPE,
                            EDIT_FORMAT,
                            EDIT_ADD_BLOCK_COMMENT,
                            EDIT_REMOVE_BLOCK_COMMENT,
                            EDIT_TOGGLE_COMMENT,
                            EDIT_FIND_AND_REPLACE,
                            EDIT_HIDE_LINE_NUMBERS,
                            EDIT_DELETE_CURRENT_LINE,
                            EDIT_GO_TO_LINE,
                            EDIT_DELETE,
                            EDIT_SELECT_ALL,
                            EDIT_MOVE_LINE_UP,
                            EDIT_MOVE_LINE_DOWN,

                            EDIT_SOURCE_GENERATE_GETTES_AND_SETTERS,
                            EDIT_SOURCE_GENERATE_CONSTRUCTOR_USING_FIELDS,

                            EDIT_REFACTOR_RENAME,

                            VIEW_PROPERTIES,
                            VIEW_GO_TO_FOLDER,
                            VIEW_PROGRESS,
                            VIEW_OUTPUT,
                            VIEW_LOG,
                            VIEW_SHOW_HIDDEN_FILES,
                            VIEW_COLLABORATION,

                            RUN_DEBUG_APPLICATION,
                            RUN_RUN_APPLICATION,
                            RUN_STOP_APPLICATION,
                            RUN_SHOW_LOGS,

                            GIT_ADD,
                            GIT_RESET,
                            GIT_REMOVE,
                            GIT_COMMIT,
                            GIT_BRANCHES,
                            GIT_MERGE,
                            GIT_RESET_INDEX,
                            GIT_CLONE_REPOSITORY,
                            GIT_DELETE_REPOSITORY,
                            GIT_SHOW_HISTORY,
                            GIT_STATUS,
                            GIT_GIT_URL_READ_ONLY,

                            GIT_REMOTE_PUSH,
                            GIT_REMOTE_FETCH,
                            GIT_REMOTE_PULL,
                            GIT_REMOTE_REMOTES,

                            PAAS_MANY_MO_ANDROID_DEPLOY,

                            PAAS_APP_FOG_CREATE_APPLICATION,
                            PAAS_APP_FOG_APPLICATIONS,
                            PAAS_APP_FOG_SWITCH_ACCOUNT,

                            PAAS_CLOUD_BEES_CREATE_APPLICATION,
                            PAAS_CLOUD_BEES_APPLICATIONS,
                            PAAS_CLOUD_BEES_SWITCH_ACCOUNT,
                            PAAS_CLOUD_BEES_CREATE_ACCOUNT,

                            PAAS_ELASTIC_BEANSTALK_CREATE_APPLICATION,
                            PAAS_ELASTIC_BEANSTALK_MANAGE_APPLICATION,
                            PAAS_ELASTIC_BEANSTALK_SWITCH_ACCOUNT,
                            PAAS_ELASTIC_BEANSTALK_EC2_MANAGEMENT,
                            PAAS_ELASTIC_BEANSTALK_S3_MANAGEMENT,

                            PAAS_GOOGLE_APP_ENGINE_UPDATE_APPLICATION,
                            PAAS_GOOGLE_APP_ENGINE_CREATE_APPLICATION,
                            PAAS_GOOGLE_APP_ENGINE_LOGIN,

                            PAAS_HEROKU_CREATE_APPLICATION,
                            PAAS_HEROKU_APPLICATIONS,
                            PAAS_HEROKU_DEPLOY_PUBLIC_KEY,
                            PAAS_HEROKU_SWITCH_ACCOUNT,

                            PAAS_OPEN_SHIFT_CHANGE_NAMESPACE,
                            PAAS_OPEN_SHIFT_APPLICATIONS,
                            PAAS_OPEN_SHIFT_UPDATE_SHH_PUBLIC_KEY,
                            PAAS_OPEN_SHIFT_SWITCH_ACCOUNT,

                            WINDOW_SHOW_VIEW_PROJECT_EXPLORE,
                            WINDOW_SHOW_VIEW_PACKAGE_EXPLORE,

                            WINDOW_NAVIGATION_NEXT_EDITOR,
                            WINDOW_NAVIGATION_PREVIOUS_EDITOR,
                            WINDOW_WELCOME,
                            WINDOW_PREFERENCES,

                            SHELL,
                            PROFILE,
                            LOGOUT};
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "IDE usage by types";
    }
}
