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

import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "script")
public class ScriptConfiguration {

    private String name;
    private String description;
    private List<ParameterConfiguration> parameters = new ArrayList<>();

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @XmlElement(name = "parameter")
    public void setParameters(List<ParameterConfiguration> parameters) {
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public List<ParameterConfiguration> getParameters() {
        return parameters;
    }

    public Map<String, Object> getParamsAsMap() {
        Map<String, Object> result = new HashMap<>(parameters.size());
        for (ParameterConfiguration parameter : parameters) {
            result.put(parameter.getKey(), parameter.getValue());
        }

        return result;
    }
}