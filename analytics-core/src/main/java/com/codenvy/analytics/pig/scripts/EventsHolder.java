/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/** @author Anatoliy Bazko */
@Singleton
public class EventsHolder {

    private static final String CONFIGURATION = "events.xml";

    private final EventHolderConfiguration configuration;

    @Inject
    public EventsHolder(XmlConfigurationManager confManager) throws IOException {
        configuration = confManager.loadConfiguration(EventHolderConfiguration.class, CONFIGURATION);
    }

    public boolean isEventExists(String eventName) {
        for (EventConfiguration eventConfiguration : configuration.getEvents()) {
            if (eventConfiguration.getName().equals(eventName)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * @return the description of the event
     * @throws IllegalArgumentException if event doesn't exist into configuration
     */
    public String getDescription(String eventName) throws IllegalArgumentException {
        for (EventConfiguration eventConfiguration : configuration.getEvents()) {
            if (eventConfiguration.getName().equals(eventName)) {
                return eventConfiguration.getDescription();
            }
        }
        
        throw new IllegalArgumentException("There is no event with name " + eventName);
    }
}
