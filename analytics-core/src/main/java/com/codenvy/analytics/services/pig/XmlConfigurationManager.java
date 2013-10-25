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
package com.codenvy.analytics.services.pig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class XmlConfigurationManager implements ConfigurationManager {

    /** Runtime parameter name. It contains the destination for configuration. */
    public static final String ANALYTICS_PIG_RUNNER_CONFIG_PROPERTY = "analytics.pig.runner.config";

    /** The value of {@value #ANALYTICS_PIG_RUNNER_CONFIG_PROPERTY}. */
    public static final String PIG_RUNNER_CONFIG = System.getProperty(ANALYTICS_PIG_RUNNER_CONFIG_PROPERTY);


    /** {@inheritDoc} */
    @Override
    public PigRunnerConfiguration loadConfiguration() throws ConfigurationManagerException {
        try (InputStream in = openResource()) {
            JAXBContext jc = JAXBContext.newInstance(PigRunnerConfiguration.class);
            return (PigRunnerConfiguration)jc.createUnmarshaller().unmarshal(in);
        } catch (JAXBException | IOException e) {
            throw new ConfigurationManagerException("Can not read the configuration from " + PIG_RUNNER_CONFIG, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void storeConfiguration() throws IOException {
        throw new UnsupportedOperationException();
    }

    protected InputStream openResource() throws ConfigurationManagerException, FileNotFoundException {
        InputStream in;

        File file = new File(PIG_RUNNER_CONFIG);
        if (file.exists()) {
            in = new FileInputStream(file);
        } else {
            in = getClass().getClassLoader().getResourceAsStream(PIG_RUNNER_CONFIG);

            if (in == null) {
                throw new ConfigurationManagerException("Resource " + PIG_RUNNER_CONFIG + " not found");
            }
        }

        return in;
    }
}
