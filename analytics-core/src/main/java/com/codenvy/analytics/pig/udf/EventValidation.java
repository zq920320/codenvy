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
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
public class EventValidation extends FilterFunc {

    private static final String NULL    = "null";
    private static final String DEFAULT = "default";
    private static final String WS      = "WS";
    private static final String USER    = "USER";

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

        boolean validated = true;
        Map<String, String> values = eventsHolder.getParametersValues(event, message);

        for (Parameter param : eventsHolder.getDefinition(event).getParameters().getParams()) {
            String paramName = param.getName();

            switch (paramName) {
                case WS:
                    validated &= !isEmptyOrDefaultValue(ws);
                    break;
                case USER:
                    validated &= !isEmptyOrDefaultValue(user);
                    break;
                default:
                    validated &= param.isAllowEmptyValue() || !isEmptyValue(values.get(paramName));
                    break;
            }
        }

        return validated;
    }

    private boolean isEmptyOrDefaultValue(String value) {
        return isEmptyValue(value) || value.equalsIgnoreCase(DEFAULT);
    }

    private boolean isEmptyValue(String value) {
        return value == null || value.equalsIgnoreCase(NULL) || value.isEmpty();
    }
}
