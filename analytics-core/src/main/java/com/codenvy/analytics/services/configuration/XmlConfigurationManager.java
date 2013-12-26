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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class XmlConfigurationManager<T> implements ConfigurationManager<T> {

    private final Class<T> clazz;
    private final String   resource;

    public XmlConfigurationManager(Class<T> clazz, String resource) {
        this.clazz = clazz;
        this.resource = resource;
    }

    /** {@inheritDoc} */
    @Override
    public T loadConfiguration() throws ConfigurationManagerException {
        try (InputStream in = openResource(resource)) {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            return (T)jc.createUnmarshaller().unmarshal(in);
        } catch (JAXBException | IOException e) {
            throw new ConfigurationManagerException("Can not read the configuration from " + resource, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void storeConfiguration(T configuration) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected InputStream openResource(String resource) throws ConfigurationManagerException, FileNotFoundException {
        InputStream in;

        File file = new File(resource);
        if (file.exists()) {
            in = new FileInputStream(file);
        } else {
            in = getClass().getClassLoader().getResourceAsStream(resource);

            if (in == null) {
                throw new ConfigurationManagerException("Resource " + resource + " not found");
            }
        }

        return in;
    }
}
