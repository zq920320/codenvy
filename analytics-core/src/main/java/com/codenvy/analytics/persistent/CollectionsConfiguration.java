/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.persistent;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
@XmlRootElement(name = "collections")
public class CollectionsConfiguration {
    private List<CollectionConfiguration> collections;

    public List<CollectionConfiguration> getCollections() {
        return collections;
    }

    @XmlElement(name = "collection")
    public void setCollections(List<CollectionConfiguration> collections) {
        this.collections = collections;
    }

    public Map<String, CollectionConfiguration> getAsMap() {
        Map<String, CollectionConfiguration> result = new LinkedHashMap<>(collections.size());
        for (CollectionConfiguration conf : collections) {
            result.put(conf.getName(), conf);
        }

        return result;
    }
}
