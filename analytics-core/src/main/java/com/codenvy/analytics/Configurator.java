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

import javax.inject.Singleton;
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
@Singleton
public class Configurator {

    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

    private static final String  CONFIGURATION     = "analytics.properties";
    private static final String  ANALYTICS_TMP_DIR = "analytics.tmp_dir";
    private static final Pattern TEMPLATE_PROP_VAR = Pattern.compile("\\$\\{([^\\}]*)\\}");
    private static final Pattern TEMPLATE_ENV_VAR  = Pattern.compile("\\$([^\\/]*)[\\/]?");

    private final Properties properties;

    public Configurator() throws IOException {
        properties = loadProperties();
    }

    public String getTmpDir() {
        return getString(ANALYTICS_TMP_DIR);
    }

    /** @return value of the property as the array of String */
    public String[] getArray(String key) {
        return exists(key) ? getString(key).split(",") : new String[0];
    }

    public boolean exists(String key) {
        return getString(key) != null;
    }

    /** @return value of the property of the String type */
    public String getString(String key) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            return null;
        }

        return replaceEnvVariables(replacePropVariables(currentValue));
    }

    private String replaceEnvVariables(String currentValue) {
        return doReplaceVariable(TEMPLATE_ENV_VAR, currentValue, new ReplaceVariableAction() {
            @Override
            public String getValue(String template) {
                boolean endsWithSeparator = template.endsWith(File.separator);

                String var = template.substring(1, template.length() - (endsWithSeparator ? 1 : 0));
                return System.getenv(var) + (endsWithSeparator ? File.separator : "");
            }
        });
    }

    private String replacePropVariables(String currentValue) {
        return doReplaceVariable(TEMPLATE_PROP_VAR, currentValue, new ReplaceVariableAction() {
            @Override
            public String getValue(String template) {
                String var = template.substring(2, template.length() - 1);
                return getString(var);
            }
        });
    }

    private String doReplaceVariable(Pattern pattern, String currentValue, ReplaceVariableAction action) {
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
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    /** @return value of the property of the int type */
    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    /** @return value of the property of the int type it exists in configuration */
    public int getInt(String key, int defaultValue) {
        return exists(key) ? Integer.parseInt(getString(key)) : defaultValue;
    }

    public Map<String, String> getAll(String keyPrefix) {
        Map<String, String> result = new HashMap<>();

        for (Object obj : properties.keySet()) {
            String key = (String)obj;

            if (key.startsWith(keyPrefix)) {
                result.put(key.substring(keyPrefix.length() + 1), getString(key));
            }
        }

        return result;
    }

    private Properties loadProperties() throws IOException {
        try {
            return loadFromResource();
        } catch (IOException e) {
            try {
                return loadFromFile(System.getenv("CODENVY_LOCAL_CONF_DIR"));
            } catch (IOException e1) {
                return loadFromFile(System.getProperty("codenvy.local.conf.dir"));
            }
        }
    }

    private Properties loadFromResource() throws IOException {
        Properties properties = new Properties();

        try (InputStream in = Configurator.class.getClassLoader().getResourceAsStream(CONFIGURATION)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found " + CONFIGURATION);
            }

            properties.load(in);
        }

        LOG.info("Configuration has been read from resource " + CONFIGURATION);
        return properties;
    }

    private Properties loadFromFile(String baseDir) throws IOException {
        Properties properties = new Properties();
        File file = new File(baseDir, CONFIGURATION);

        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(in);
        }

        LOG.info("Configuration has been read from file " + file.getCanonicalPath());
        return properties;
    }

    private interface ReplaceVariableAction {
        String getValue(String template);
    }
}
