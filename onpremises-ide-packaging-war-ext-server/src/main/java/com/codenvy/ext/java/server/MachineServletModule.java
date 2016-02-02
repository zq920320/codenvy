/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.ext.java.server;


import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;

/**
 * @author Sergii Kabashniuk
 * @author Max Shaposhnik
 * @author Alexander Garagatyi
 */
@DynaModule
public class MachineServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        //listeners
        getServletContext().addListener(new org.everrest.websockets.WSConnectionTracker());
        getServletContext().addListener(new com.codenvy.auth.sso.client.DestroySessionListener());
        //filters
        filter("/*").through(com.codenvy.auth.sso.client.MachineRequestTokenInjectFilter.class);
        filterRegex("/(?!_sso/).*$").through(com.codenvy.auth.sso.client.LoginFilter.class);
        //servlets
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
        serveRegex("^/ext((?!(/(ws|eventbus)($|/.*)))/.*)").with(org.everrest.guice.servlet.GuiceEverrestServlet.class);
    }
}
