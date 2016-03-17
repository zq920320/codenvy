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

import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractRecipientGroup implements RecipientGroup {

    protected final List<ParameterConfiguration> parameters;

    public AbstractRecipientGroup(List<ParameterConfiguration> parameters) {
        this.parameters = parameters;
    }

    /** @return all parameters for specific key */
    protected Set<String> getParameters(String key) {
        Set<String> items = new HashSet<>();

        for (ParameterConfiguration parameter : parameters) {
            if (parameter.getKey().equalsIgnoreCase(key)) {
                items.add(parameter.getValue());
            }
        }

        return items;
    }

    /** @return the first occurred parameter for specific key */
    protected String getFirstParameter(String key) {
        for (ParameterConfiguration parameter : parameters) {
            if (parameter.getKey().equalsIgnoreCase(key)) {
                return parameter.getValue();
            }
        }

        return null;
    }
}
