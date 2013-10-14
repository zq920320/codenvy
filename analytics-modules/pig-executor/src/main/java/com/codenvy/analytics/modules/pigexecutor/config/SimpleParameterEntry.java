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
package com.codenvy.analytics.modules.pigexecutor.config;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class SimpleParameterEntry {

    private String name;

    private String value;

    public SimpleParameterEntry() {
    }

    public SimpleParameterEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns name.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns value.
     * 
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     * 
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
