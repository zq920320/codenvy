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

import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Anatoliy Bazko */
@Singleton
public class EventsHolder {

    public static final String IDE_CLOSED            = "ide-closed";
    public static final String IDE_OPENED            = "ide-opened";
    public static final String NOT_FACTORY_SESSIONS  =
            ReadBasedMetric.EXCLUDE_SIGN
            + "session-factory-stopped" + ReadBasedMetric.SEPARATOR + "session-factory-started";
    public static final String USER_SSO_LOGOUT_EVENT = "user-sso-logged-out";
    public static final String USER_IDLE_EVENT       = "idle";

    private static final String CONFIGURATION = "events.xml";
    private static final String VALUE_PATTERN = "#([^\\s#][^#]*|)#";

    private final EventHolderConfiguration configuration;

    private final Map<String, EventConfiguration> eventsMap;

    @Inject
    public EventsHolder(XmlConfigurationManager confManager) throws IOException {
        configuration = confManager.loadConfiguration(EventHolderConfiguration.class, CONFIGURATION);
        eventsMap = configuration.getAsMap();
    }

    public boolean isEventExists(String eventName) {
        return eventsMap.containsKey(eventName);
    }

    /**
     * Extracts all available params out of the message.
     * USER and WS parameters will be skipped.
     */
    public Map<String, String> getParametersValues(String eventName, String message) throws IllegalArgumentException {
        Map<String, String> result = new LinkedHashMap<>();

        EventConfiguration definition = getDefinition(eventName);
        for (Parameter param : definition.getParameters().getParams()) {
            String paramName = param.getName();
            String paramValue = getParameterValue(paramName, message);

            if (paramValue != null) {
                result.put(paramName, paramValue);
            }
        }

        result.remove("USER");
        result.remove("WS");

        return result;
    }

    private String getParameterValue(String paramName, String message) {
        Pattern p = Pattern.compile(paramName + VALUE_PATTERN);
        Matcher m = p.matcher(message);

        return m.find() ? m.group(1) : null;
    }

    /**
     * @return EventConfiguration
     * @throws IllegalArgumentException
     *         if event doesn't exist into configuration
     */
    public EventConfiguration getDefinition(String eventName) throws IllegalArgumentException {
        if (eventsMap.containsKey(eventName)) {
            return eventsMap.get(eventName);
        }

        throw new IllegalArgumentException("There is no event with name " + eventName);
    }

    /**
     * @return the description of the event
     * @throws IllegalArgumentException
     *         if event doesn't exist into configuration
     */
    public String getDescription(String eventName) throws IllegalArgumentException {
        if (eventsMap.containsKey(eventName)) {
            return eventsMap.get(eventName).getDescription();
        }

        throw new IllegalArgumentException("There is no event with name " + eventName);
    }
}
