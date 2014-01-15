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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractRecipientGroup implements RecipientGroup {

    protected final Map<String, String> parameters;

    public AbstractRecipientGroup(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /** @return all parameters for specific key */
    protected Set<String> getParameters(String key) {
        Set<String> items = new HashSet<>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                items.add(entry.getValue());
            }
        }

        return items;
    }
}
