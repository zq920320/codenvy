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
package com.codenvy.migration;

import com.codenvy.api.project.server.Builders;
import com.codenvy.api.project.server.ProjectJson2;
import com.codenvy.api.project.server.Runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.exists;


/**
 * Helper for C2 to C3 project conversion. Maps C2 project type to appropriate
 * C3 project properties object, or produce docker file for unsupported types.
 */
public class ProjectTypeHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectTypeHelper.class);

    private static String TEMPLATE_PHP;
    private static String TEMPLATE_JS;
    private static String TEMPLATE_SPRING;
    private static String TEMPLATE_ROR;
    private static String TEMPLATE_PY;
    private static String TEMPLATE_NJS;

    static {
        try {
            String conf = System.getProperty("codenvy.local.conf.dir");
            TEMPLATE_PHP = readFile(conf + "php.template", Charset.defaultCharset());
            TEMPLATE_JS = readFile(conf + "js.template", Charset.defaultCharset());
            TEMPLATE_SPRING = readFile(conf + "spring.template", Charset.defaultCharset());
            TEMPLATE_ROR = readFile(conf + "ror.template", Charset.defaultCharset());
            TEMPLATE_PY = readFile(conf + "python.template", Charset.defaultCharset());
            TEMPLATE_NJS = readFile(conf + "nodejs.template", Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static final String PROJECT_TYPE_MAVEN = "maven";
    private static final String PROJECT_TYPE_BLANK = "blank";

    public static ProjectJson2 projectTypeToDescription(String projectType) {
        ProjectJson2 projectDescription = new ProjectJson2();
        Map<String, List<String>> outputAttrs = new HashMap<>();
        if (projectType != null) {
            switch (projectType) {
                case "Jar": {
                    projectDescription.setType(PROJECT_TYPE_MAVEN);
                    projectDescription.setBuilders(new Builders("maven"));
                    projectDescription.setRunners(new Runners("java-standalone-default"));
                    break;
                }
                case "Servlet/JSP": {
                    projectDescription.setType(PROJECT_TYPE_MAVEN);
                    projectDescription.setBuilders(new Builders("maven"));
                    projectDescription.setRunners(new Runners("java-webapp-default"));
                    break;
                }
                case "PHP": {
                    projectDescription.setType("php");
                    break;
                }
                case "JavaScript": {
                    projectDescription.setType(PROJECT_TYPE_BLANK);
                    break;
                }
                case "Spring": {
                    projectDescription.setType(PROJECT_TYPE_MAVEN);
                    projectDescription.setBuilders(new Builders("maven"));
                    projectDescription.setRunners(new Runners("java-webapp-default"));
                    break;
                }
                case "Rails": {
                    projectDescription.setType("ruby");
                    break;
                }
                case "Python": {
                    projectDescription.setType("python");
                    break;
                }
                case "Android": {
                    projectDescription.setType(PROJECT_TYPE_BLANK);
                    projectDescription.setBuilders(new Builders("maven"));
                    projectDescription.setRunners(new Runners("java-mobile-android"));
                    break;
                }
                case "nodejs": {
                    projectDescription.setType(PROJECT_TYPE_BLANK);
                    break;
                }
                default: {
                    projectDescription.setType(PROJECT_TYPE_BLANK);
                }
            }
        } else {
            projectDescription.setType(PROJECT_TYPE_BLANK);
        }
        projectDescription.setAttributes(outputAttrs);
        return projectDescription;
    }

    public static String getRunnerTemplate(String projectType) {
        if (projectType == null || projectType.isEmpty()) {
            return null;
        }

        switch (projectType) {
            case "PHP": {
                return TEMPLATE_PHP;
            }
            case "JavaScript": {
                return TEMPLATE_JS;
            }
            case "Spring": {
                return TEMPLATE_SPRING;
            }
            case "Rails": {
                return TEMPLATE_ROR;
            }
            case "Python": {
                return TEMPLATE_PY;
            }
            case "nodejs": {
                return TEMPLATE_NJS;
            }
            default: {
                return null;
            }
        }
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        if (exists(Paths.get(path))) {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } else {
            return null;
        }
    }
}
