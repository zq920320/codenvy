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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author Anatoliy Bazko */
public class EventsHolder {

    private static final Logger LOG           = LoggerFactory.getLogger(EventsHolder.class);
    private static final String CONFIGURATION = "events.xml";

    private static final EventHolderConfiguration configuration;

    static {
        try {
            XmlConfigurationManager<EventHolderConfiguration> configurationManager =
                    new XmlConfigurationManager<>(EventHolderConfiguration.class, CONFIGURATION);

            configuration = configurationManager.loadConfiguration();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    public static boolean isEventExists(String eventName) {
        for (EventConfiguration eventConfiguration : configuration.getEvents()) {
            if (eventConfiguration.getName().equals(eventName)) {
                return true;
            }
        }

        return false;
    }
}
