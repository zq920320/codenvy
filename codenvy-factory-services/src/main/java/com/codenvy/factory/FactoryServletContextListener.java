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
import com.codenvy.factory.storage.InMemoryFactoryStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** Listener for using {@link InMemoryFactoryStore} as {@link FactoryStore} backend. */
public class FactoryServletContextListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(FactoryServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();
        sctx.setAttribute(FactoryStore.class.getName(), new InMemoryFactoryStore());
        LOG.info("InMemoryFactoryStore backend for factory storage loaded.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();
        sctx.removeAttribute(FactoryStore.class.getName());
    }
}