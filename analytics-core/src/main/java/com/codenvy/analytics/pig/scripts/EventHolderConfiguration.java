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
package com.codenvy.analytics.pig.scripts;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "events")
public class EventHolderConfiguration {

    private List<EventConfiguration> events;

    public List<EventConfiguration> getEvents() {
        return events;
    }

    @XmlElement(name = "event")
    public void setEvents(List<EventConfiguration> events) {
        this.events = events;
    }

    public Map<String, EventConfiguration> getAsMap() {
        Map<String, EventConfiguration> map = new LinkedHashMap<>();

        for (EventConfiguration eventConfiguration : events) {
            map.put(eventConfiguration.getName(), eventConfiguration);
        }
        return map;
    }
}
