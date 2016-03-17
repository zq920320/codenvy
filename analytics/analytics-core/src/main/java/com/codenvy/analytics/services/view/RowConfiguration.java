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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@XmlRootElement(name = "row")
public class RowConfiguration {

    private String clazz;
    private List<ParameterConfiguration> parameters = new ArrayList<>();

    @XmlAttribute(name = "class")
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @XmlElement(name = "parameter")
    public void setParameters(List<ParameterConfiguration> parameters) {
        this.parameters = parameters;
    }

    public String getClazz() {
        return clazz;
    }

    public List<ParameterConfiguration> getParameters() {
        return parameters;
    }

    public Map<String, String> getParamsAsMap() {
        Map<String, String> result = new HashMap<>(parameters.size());
        for (ParameterConfiguration parameter : parameters) {
            result.put(parameter.getKey(), parameter.getValue());
        }

        return result;
    }
}
