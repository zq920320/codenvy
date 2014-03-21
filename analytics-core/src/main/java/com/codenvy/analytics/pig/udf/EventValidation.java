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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.pig.scripts.EventsHolder;
import com.codenvy.analytics.pig.scripts.Parameter;

import org.apache.pig.FilterFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Reshetnyak
 */
public class EventValidation extends FilterFunc {

    private static final String NULL = "null";
    private static final String DEFAULT = "default";
    private static final String WS = "WS";
    private static final String USER = "USER";
    private static final String EMPTY = "";
    private static final String VALUE_PATTERN = "#([^\\s#][^#]*|)";

    private final EventsHolder eventsHolder;

    public EventValidation() {
        eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Override
    public Boolean exec(Tuple input) throws IOException {

        String event = (String)input.get(0);
        String ws = (String)input.get(1);
        String user = (String)input.get(2);
        String message = (String)input.get(3);


        if (!eventsHolder.isEventExists(event)) {
            return false;
        }

        for (Parameter param : eventsHolder.getDefinition(event).getParameters().getParams()) {

            if (WS.equals(param.getName()) && !validateValue(ws, param)) {
                return false;
            } else if (USER.equals(param.getName()) && !validateValue(user, param)) {
                return false;
            } else if (isEmptyValue(param.getName(), message) && !param.isAllowEmptyValue()) {
                return false;
            }
        }

        return true;
    }

    private boolean validateValue(String value, Parameter param) {
        if (value.equalsIgnoreCase(NULL) ||
            (value.equalsIgnoreCase(DEFAULT) && !param.isAllowEmptyValue()) ||
            (value.equals(EMPTY) && !param.isAllowEmptyValue())) {
            return false;
        }
        return true;
    }

    private boolean isEmptyValue(String parameterName, String message) {
        Pattern p = Pattern.compile(parameterName + VALUE_PATTERN);
        Matcher m = p.matcher(message);

        if (m.find()) {
            String value = m.group().replace(parameterName + "#", EMPTY);
           return  value.equalsIgnoreCase(NULL) ? true : value.equals(EMPTY);
        }

        return false;
    }
}
