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
import java.util.Properties;

/**
 * It is supposed to keep configuration of all services in one place.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Configurator {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

    /** System property. Contains the Cassandra keyspace where analytics data will be stored. */
    private static final String ANALYTICS_CONFIGURATION_DIRECTORY_PROPERTY = "analytics.configuration.dir";

    /** The value of {@value #ANALYTICS_CONFIGURATION_DIRECTORY_PROPERTY}. */
    public static final String CONFIGURATION_DIRECTORY = System.getProperty(ANALYTICS_CONFIGURATION_DIRECTORY_PROPERTY);

    private static final String RESOURCE = "analytics.conf";

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
        return (String)properties.get(key);
    }

    /** @return value of the property of the int type */
    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    private static void loadFromResource() throws IOException {
        try (InputStream in = Configurator.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found " + RESOURCE);
            }

            properties.load(in);
            LOG.info("Configuration has been read from resource " + RESOURCE);
        }
    }

    private static void loadFromFile() throws IOException {
        File file = new File(CONFIGURATION_DIRECTORY, RESOURCE);

        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(in);
            LOG.info("Configuration has been read from file " + file.getCanonicalPath());
        }
    }
}
