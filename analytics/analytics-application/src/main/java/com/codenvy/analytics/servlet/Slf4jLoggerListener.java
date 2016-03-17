/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

package com.codenvy.analytics.servlet;

import ch.qos.logback.classic.LoggerContext;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ServletContextListener is used to terminate all works inside LoggerContext.
 *
 * @author Anatoliy Bazko
 */
public class Slf4jLoggerListener implements ServletContextListener {

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            LoggerContext ctx = (LoggerContext)factory;
            ctx.stop();
        }
    }
}
