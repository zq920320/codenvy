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
package com.codenvy.analytics.services.reports;

import javax.xml.bind.annotation.XmlElement;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractFrequencyConfiguration {

    private ViewsConfiguration              views;
    private ContextInitializerConfiguration contextInitializer;

    @XmlElement(name = "views")
    public void setViews(ViewsConfiguration views) {
        this.views = views;
    }

    public ViewsConfiguration getViews() {
        return views;
    }

    @XmlElement(name = "context-initializer")
    public void setContextInitializer(ContextInitializerConfiguration contextInitializer) {
        this.contextInitializer = contextInitializer;
    }

    public ContextInitializerConfiguration getContextInitializer() {
        return contextInitializer;
    }
}
