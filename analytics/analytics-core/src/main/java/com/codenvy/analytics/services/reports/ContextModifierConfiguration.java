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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.services.configuration.ParametersConfiguration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@XmlRootElement(name = "context-modifier")
public class ContextModifierConfiguration {

    private String                  clazz;
    private ParametersConfiguration parametersConfiguration;

    @XmlElement(name = "class")
    public void setClazz(String contextInitializer) {
        this.clazz = contextInitializer;
    }

    public String getClazz() {
        return clazz;
    }

    public ParametersConfiguration getParametersConfiguration() {
        return parametersConfiguration;
    }

    @XmlElement(name = "parameters")
    public void setParametersConfiguration(ParametersConfiguration parametersConfiguration) {
        this.parametersConfiguration = parametersConfiguration;
    }
}
