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
package com.codenvy.analytics.services.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "parameters")
public class ParametersConfiguration {

    private List<ParameterConfiguration> parameters;

    public List<ParameterConfiguration> getParameters() {
        return parameters;
    }

    @XmlElement(name = "parameter")
    public void setParameters(List<ParameterConfiguration> usersGroup) {
        this.parameters = usersGroup;
    }

    public Map<String, String> getParamsAsMap() {
        Map<String, String> result = new HashMap<>(parameters.size());
        for (ParameterConfiguration param : parameters) {
            result.put(param.getKey(), param.getValue());
        }

        return result;
    }
}
