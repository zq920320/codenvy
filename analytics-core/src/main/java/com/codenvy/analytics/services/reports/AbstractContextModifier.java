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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractContextModifier implements ContextModifier {

    protected final List<ParameterConfiguration> parameters;

    public AbstractContextModifier(List<ParameterConfiguration> parameters) {
        this.parameters = parameters;
    }

    /** @return all parameters for specific key */
    protected Set<String> getParameters(String key) {
        Set<String> items = new LinkedHashSet<>();

        for (ParameterConfiguration parameter : parameters) {
            if (parameter.getKey().equalsIgnoreCase(key)) {
                items.add(parameter.getValue());
            }
        }

        return items;
    }
}
