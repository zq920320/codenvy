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
package com.codenvy.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It is supposed to keep configuration of all services in one place.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Configurator {

    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

    private static final String  CONFIGURATION      = "analytics.conf";
    private static final String  ANALYTICS_CONF_DIR = System.getProperty("codenvy.local.conf.dir");
    private static final Pattern TEMPLATE_PROP_VAR  = Pattern.compile("\\$\\{([^\\}]*)\\}");
    private static final Pattern TEMPLATE_ENV_VAR   = Pattern.compile("\\$([^\\/]*)[\\/]?");

    private static Properties properties = new Properties();

    static {
        try {
            loadFromResource();
        } catch (IOException e) {
            try {
                loadFromFile();
            } catch (IOException e1) {
                throw new IllegalStateException(
                        "Configurator can't be instantiated. There is no configuration to read from");
            }
        }
    }


    /** @return value of the property as the array of String */
    public static String[] getArray(String key) {
        return getString(key).split(",");
    }

    /** @return value of the property of the String type */
    public static String getString(String key) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            return null;
        }

        return replaceEnvVariables(replacePropVariables(currentValue));
    }

    private static String replaceEnvVariables(String currentValue) {
        return doReplaceVariable(TEMPLATE_ENV_VAR, currentValue, new ReplaceVariableAction() {
            @Override
            public String getValue(String template) {
                boolean endsWithSeparator = template.endsWith(File.separator);

                String var = template.substring(1, template.length() - (endsWithSeparator ? 1 : 0));
                return System.getenv(var) + (endsWithSeparator ? File.separator : "");
            }
        });
    }

    private static String replacePropVariables(String currentValue) {
        return doReplaceVariable(TEMPLATE_PROP_VAR, currentValue, new ReplaceVariableAction() {
            @Override
            public String getValue(String template) {
                String var = template.substring(2, template.length() - 1);
                return getString(var);
            }
        });
    }

    private static String doReplaceVariable(Pattern pattern, String currentValue, ReplaceVariableAction action) {
        int lastPos = 0;
        StringBuilder value2return = new StringBuilder();

        Matcher matcher = pattern.matcher(currentValue);
        while (matcher.find()) {
            value2return.append(currentValue.substring(lastPos, matcher.start()));

            String template = currentValue.substring(matcher.start(), matcher.end());
            value2return.append(action.getValue(template));

            lastPos = matcher.end();
        }
        value2return.append(currentValue.substring(lastPos));

        return value2return.toString();
    }

    /** @return value of the property of the boolean type */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    /** @return value of the property of the int type */
    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static Map<String, String> getAll(String keyPrefix) {
        Map<String, String> result = new HashMap<>();

        for (Object obj : properties.keySet()) {
            String key = (String)obj;

            if (key.startsWith(keyPrefix)) {
                result.put(key.substring(keyPrefix.length() + 1), getString(key));
            }
        }

        return result;
    }

    private static void loadFromResource() throws IOException {
        try (InputStream in = Configurator.class.getClassLoader().getResourceAsStream(CONFIGURATION)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found " + CONFIGURATION);
            }

            properties.load(in);
            LOG.info("Configuration has been read from resource " + CONFIGURATION);
        }
    }

    private static void loadFromFile() throws IOException {
        File file = new File(ANALYTICS_CONF_DIR, CONFIGURATION);

        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(in);
            LOG.info("Configuration has been read from file " + file.getCanonicalPath());
        }
    }

    private interface ReplaceVariableAction {
        String getValue(String template);
    }
}
