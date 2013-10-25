/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.factory;

import com.codenvy.api.factory.FactoryStore;
import com.codenvy.commons.json.JsonHelper;
import com.codenvy.commons.json.JsonParseException;
import com.codenvy.factory.storage.InMemoryFactoryStore;
import com.codenvy.factory.storage.mongo.MongoDBFactoryStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Listener that choose backend for {@link FactoryStore}.
 * If MongoDb configuration exist and it's correct {@link MongoDBFactoryStore} is used. {@link InMemoryFactoryStore} is used otherwise.
 */
public class FactoryFallBackServletContextListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryFallBackServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();

        sctx.setAttribute(FactoryStore.class.getName(), getFactoryStore());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();
        sctx.removeAttribute(FactoryStore.class.getName());
    }

    private FactoryStore getFactoryStore() {
        FactoryStore factoryStore;

        File dbSettings = new File(new File(System.getProperty("codenvy.local.conf.dir")), "factory-storage-configuration.json");
        if (dbSettings.exists() && !dbSettings.isDirectory()) {
            try (InputStream is = new FileInputStream(dbSettings)) {
                MongoDbConfiguration mConf = JsonHelper.fromJson(is, MongoDbConfiguration.class, null);
                factoryStore = new MongoDBFactoryStore(mConf);
            } catch (IOException | JsonParseException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new RuntimeException("Invalid mongo database configuration : " + dbSettings.getAbsolutePath());
            }
        } else {
            factoryStore = new InMemoryFactoryStore();
        }

        LOG.info("{} backend for factory storage loaded.", factoryStore.getClass().getName());

        return factoryStore;
    }
}