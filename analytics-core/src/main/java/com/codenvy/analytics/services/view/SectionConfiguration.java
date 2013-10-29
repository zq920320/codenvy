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
package com.codenvy.analytics.services.view;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "section")
public class SectionConfiguration {

    private String name;

    private int length;

    private List<RowConfiguration> rows;

    @XmlAttribute
    public void setLength(int length) {
        this.length = length;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @XmlElement (name = "row")
    public void setRows(List<RowConfiguration> rows) {
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public List<RowConfiguration> getRows() {
        return rows;
    }
}
