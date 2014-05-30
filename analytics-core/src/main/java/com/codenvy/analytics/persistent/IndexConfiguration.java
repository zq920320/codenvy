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
package com.codenvy.analytics.persistent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
@XmlRootElement(name = "index")
public class IndexConfiguration {
    private String  name;
    private boolean fixed;
    private List<FieldConfiguration> fields = new ArrayList<>();

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "field")
    public void setFields(List<FieldConfiguration> fields) {
        this.fields = fields;
    }

    public List<FieldConfiguration> getFields() {
        return this.fields;
    }

    @XmlAttribute(name = "fixed")
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }


}