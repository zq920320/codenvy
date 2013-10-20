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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "script")
public class ScriptConfiguration {

    private String name;

    private java.util.Map parameters = new HashMap<>();

    /** Setter for {@link #name} */
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    /** Setter for {@link #parameters} */
    @XmlElement
    @XmlJavaTypeAdapter(HashMapAdapter.class)
    public void setParameters(java.util.Map parameters) {
        this.parameters = parameters;
    }

    /** Getter for {@link #name} */
    public String getName() {
        return name;
    }

    /** Getter for {@link #parameters} */
    public java.util.Map getParameters() {
        return parameters;
    }

    public static class HashMapAdapter extends XmlAdapter<Map, java.util.Map> {
        @Override
        public Map marshal(java.util.Map map) {
            Map mapType = new Map();
            for (Entry<String, String> entry : map.entrySet()) {
                Entry mapEntry = new Entry();
                mapEntry.key = entry.getKey();
                mapEntry.value = entry.getValue();
                mapType.entry.add(mapEntry);
            }
            return mapType;
        }

        @Override
        public java.util.Map unmarshal(Map type) throws Exception {
            java.util.Map map = new HashMap<>();
            for (Entry entry : type.entry) {
                map.put(entry.key, entry.value);
            }
            return map;
        }
    }

    public static class Map {
        public List<Entry> entry = new ArrayList<>();
    }

    public static class Entry {
        public String key;
        public String value;
    }
}
