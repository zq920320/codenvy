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

package com.codenvy.analytics.metrics;

import java.io.File;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum MetricFilter {
    WS,
    USERS,
    DOMAINS,
    COMPANY,
    REFERRER_URL {
        @Override
        public String translateToRelativePath(String value) {
            return super.translateToRelativePath("" + value.hashCode());
        }
    },
    FACTORY_URL {
        @Override
        public String translateToRelativePath(String value) {
            return super.translateToRelativePath("" + value.hashCode());
        }
    };

    /** @return true if context contains given parameter */
    public boolean exists(Map<String, String> context) {
        return context.get(name()) != null;
    }

    /** Puts value into execution context */
    public void put(Map<String, String> context, String value) {
        context.put(name(), value);
    }

    /** Removes filter from context */
    public void remove(Map<String, String> context) {
        context.remove(name());
    }

    /** Gets value from execution context */
    public String get(Map<String, String> context) {
        return context.get(name());
    }

    /** Translates value to relative path */
    public String translateToRelativePath(String value) {
        if (value.length() < 3) {
            return value;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(value.substring(0, 1));
        builder.append(File.separatorChar);

        builder.append(value.substring(1, 2));
        builder.append(File.separatorChar);

        builder.append(value.substring(2, 3));
        builder.append(File.separatorChar);

        builder.append(value.substring(3));

        return builder.toString();
    }
}
