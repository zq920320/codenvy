/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.api;

import com.codenvy.inject.DynaModule;
import com.google.inject.servlet.ServletModule;

import org.everrest.guice.servlet.GuiceEverrestServlet;

/** @author Anatoliy Bazko */
@DynaModule
public class ApiServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        serve("/api/*").with(GuiceEverrestServlet.class);
    }
}
