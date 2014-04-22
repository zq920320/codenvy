/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.metrics.AbstractMapValueResulted;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class ProjectTypes extends AbstractMapValueResulted {

    public static final String JAR           = "jar";
    public static final String JSP           = "servlet/jsp";
    public static final String DJANGO        = "django";
    public static final String WAR           = "war";
    public static final String JAVA          = "java";
    public static final String MMP           = "maven multi-module";
    public static final String SPRING        = "spring";
    public static final String NODE_JS       = "nodejs";
    public static final String PHP           = "php";
    public static final String PYTHON        = "python";
    public static final String ANDROID       = "android";
    public static final String GOOGLE_MBS    = "google-mbs-client-android";
    public static final String OTHER_NULL    = "null";
    public static final String OTHER_DEFAULT = "default";
    public static final String OTHER_SERV    = "serv";
    public static final String OTHER_EXO     = "exo";
    public static final String RUBY          = "ruby";
    public static final String RAILS         = "rails";
    public static final String JAVA_SCRIPT   = "javascript";

    public static final String[] TYPES =
            {JAR, JSP, DJANGO, WAR, JAVA, MMP, SPRING, NODE_JS, PHP, PYTHON,
             ANDROID, GOOGLE_MBS, OTHER_NULL, OTHER_DEFAULT, OTHER_SERV, OTHER_EXO,
             RUBY, RAILS, JAVA_SCRIPT};

    public ProjectTypes(String metricName) {
        super(metricName);
    }

    public ProjectTypes() {
        super(MetricType.PROJECT_TYPES);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{JAR,
                            JSP,
                            DJANGO,
                            WAR,
                            JAVA,
                            MMP,
                            SPRING,
                            NODE_JS,
                            PHP,
                            PYTHON,
                            ANDROID,
                            GOOGLE_MBS,
                            OTHER_DEFAULT,
                            OTHER_EXO,
                            OTHER_NULL,
                            OTHER_SERV,
                            RUBY,
                            RAILS,
                            JAVA_SCRIPT};
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Created projects by types";
    }
}
