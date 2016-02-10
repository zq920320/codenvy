/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy
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
package com.codenvy.plugin;

import com.atlassian.sal.api.ApplicationProperties;

/**
 * Implementation of {@link com.codenvy.plugin.CodenvyPluginComponent}
 *
 * @author Stephane Tournie
 */
public class CodenvyPluginComponentImpl implements CodenvyPluginComponent {
    private final ApplicationProperties applicationProperties;

    public CodenvyPluginComponentImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Get the name of the component
     *
     * @return the name of the component
     */
    public String getName() {
        if (null != applicationProperties) {
            return "codenvyPluginComponent:" + applicationProperties.getDisplayName();
        }

        return "codenvyPluginComponent";
    }
}
