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
public class XmlConfigurationManager {

    private static final Logger LOG = LoggerFactory.getLogger(XmlConfigurationManager.class);

    public <T> T loadConfiguration(Class<?> clazz, String resource) throws IOException {
        try (InputStream in = openResource(clazz, resource)) {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            return (T)jc.createUnmarshaller().unmarshal(in);
        } catch (JAXBException | IOException e) {
            throw new IOException("Can not read the configuration from " + resource, e);
        }
    }

    protected InputStream openResource(Class<?> clazz, String resource) throws IOException {
        File file = new File(resource);
        if (file.exists()) {
            LOG.info("Configuration " + clazz.getName() + " is found in " + file.getPath());
            return new FileInputStream(file);

        } else {
            URL url = getClass().getClassLoader().getResource(resource);
            if (url == null) {
                throw new IOException("Resource " + resource + " not found");
            }

            LOG.info("Configuration " + clazz.getName() + " is found in " + url.getPath());
            return url.openStream();
        }
    }
}
