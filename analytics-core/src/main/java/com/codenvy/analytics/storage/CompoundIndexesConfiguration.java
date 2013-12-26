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
package com.codenvy.analytics.storage;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
@XmlRootElement(name = "compound-indexes")
public class CompoundIndexesConfiguration {

    private List<CompoundIndexConfiguration> compoundIndexes;
   
    @XmlElement(name = "compound-index")
    public void setCompoundIndexes(List<CompoundIndexConfiguration> compoundIndexes) {
        this.compoundIndexes = compoundIndexes;
    }
    
    public List<CompoundIndexConfiguration> getCompoundIndexes() {
        return compoundIndexes;
    }
}
