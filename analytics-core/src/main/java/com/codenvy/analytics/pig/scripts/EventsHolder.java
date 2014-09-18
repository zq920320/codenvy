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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.fetchEncodedPairs;

/** @author Anatoliy Bazko */
@Singleton
public class EventsHolder {

    public static final String IDE_CLOSED            = "ide-closed";
    public static final String IDE_OPENED            = "ide-opened";
    public static final String USER_SSO_LOGOUT_EVENT = "user-sso-logged-out";
    public static final String USER_IDLE_EVENT       = "idle";

    private static final String CONFIGURATION = "events.xml";
    private static final String VALUE_PATTERN = "#([^\\s#][^#]*|)#";

    private final Map<String, EventConfiguration> eventsMap;

    @Inject
    public EventsHolder(XmlConfigurationManager confManager) throws IOException {
        EventHolderConfiguration configuration = confManager.loadConfiguration(EventHolderConfiguration.class, CONFIGURATION);
        eventsMap = configuration.getAsMap();
    }

    public boolean isEventExists(String eventName) {
        return eventsMap.containsKey(eventName);
    }

    public Collection<EventConfiguration> getAvailableEvents() {
        return eventsMap.values();
    }

    /**
     * Extracts all available params out of the message.
     * USER and WS parameters will be skipped.
     */
    public Map<String, Object> getParametersValues(String eventName, String message) throws IllegalArgumentException {
        Map<String, Object> result = new LinkedHashMap<>();

        EventConfiguration definition = getDefinition(eventName);
        for (Parameter param : definition.getParameters().getParams()) {
            String paramName = param.getName();
            Object paramValue = castType(getParameterValue(paramName, message), param.getType());

            if (paramValue != null) {
                result.put(paramName, paramValue);
            }
        }

        String paramValue = getParameterValue("PARAMETERS", message);
        try {
            if (paramValue != null) {
                result.putAll(fetchEncodedPairs(paramValue, false));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        result.remove("USER");
        result.remove("WS");

        return result;
    }

    private Object castType(@Nullable String value, @Nullable String type) {
        if (value == null) {
            return null;
        }

        if ("String".equalsIgnoreCase(type)) {
            return value;
        } else if ("Long".equalsIgnoreCase(type)) {
            return Long.valueOf(value);
        }

        return value;
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
