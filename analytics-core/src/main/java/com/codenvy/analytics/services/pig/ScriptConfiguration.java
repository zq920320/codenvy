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
package com.codenvy.analytics.services.pig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "script")
public class ScriptConfiguration {

    private String name;

    private Map<String, String> parameters = new HashMap<>();

    /** Setter for {@link #name} */
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    /** Setter for {@link #parameters} */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /** Getter for {@link #name} */
    public String getName() {
        return name;
    }

    /** Getter for {@link #parameters} */
    public Map<String, String> getParameters() {
        return parameters;
    }
}
