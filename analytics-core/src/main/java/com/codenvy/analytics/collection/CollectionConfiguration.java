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
package com.codenvy.analytics.collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
@XmlRootElement(name = "collection")
public class CollectionConfiguration {
    private String name;
    private List<CompoundIndexConfiguration> compoundIndexes = new ArrayList<>();

    @XmlElement(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @XmlElement(name = "compound-index")
    public void setCompoundIndexes(List<CompoundIndexConfiguration> compoundIndexes) {
        this.compoundIndexes = compoundIndexes;
    }

    public List<CompoundIndexConfiguration> getCompoundIndexes() {
        return compoundIndexes;
    }
}



