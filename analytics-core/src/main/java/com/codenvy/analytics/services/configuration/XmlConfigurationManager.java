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
package com.codenvy.analytics.services.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class XmlConfigurationManager<T> implements ConfigurationManager<T> {

    private static final Logger LOG = LoggerFactory.getLogger(XmlConfigurationManager.class);

    private final Class<T> clazz;
    private final String   resource;

    public XmlConfigurationManager(Class<T> clazz, String resource) {
        this.clazz = clazz;
        this.resource = resource;
    }

    @Override
    public T loadConfiguration() throws ConfigurationManagerException {
        try (InputStream in = openResource(resource)) {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            return (T)jc.createUnmarshaller().unmarshal(in);
        } catch (JAXBException | IOException e) {
            throw new ConfigurationManagerException("Can not read the configuration from " + resource, e);
        }
    }

    @Override
    public void storeConfiguration(T configuration) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected InputStream openResource(String resource) throws IOException {
        File file = new File(resource);
        if (file.exists()) {
            LOG.info("Configuration is found in " + file.getPath());
            return new FileInputStream(file);

        } else {
            URL url = getClass().getClassLoader().getResource(resource);
            if (url == null) {
                throw new ConfigurationManagerException("Resource " + resource + " not found");
            }

            LOG.info("Configuration is found in " + url.getPath());
            return url.openStream();
        }
    }
}
