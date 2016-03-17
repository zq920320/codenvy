/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.metrics.projects.AbstractProjectPaas;
import com.codenvy.analytics.metrics.projects.AbstractProjectType;
import com.codenvy.analytics.pig.scripts.EventsHolder;
import com.codenvy.analytics.pig.scripts.Parameter;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
public class EventValidation extends EvalFunc<String> {

    private static final String NULL    = "null";
    private static final String DEFAULT = "default";
    private static final String WS      = "WS";
    private static final String USER    = "USER";

    public static final String PAAS = "PAAS";
    public static final String TYPE = "TYPE";

    public static final String EVENTS_DOESNT_EXIST  = "Events doesn't exist";
    public static final String WORKSPACE_IS_EMPTY   = "Workspace is empty";
    public static final String USER_IS_EMPTY        = "User is empty";
    public static final String VALUE_IS_NOT_ALLOWED = "Is not allowed value '%s' for '%s' parameter";

    private final EventsHolder eventsHolder;

    public EventValidation() {
        eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Override
    public String exec(Tuple input) throws IOException {
        String event = (String)input.get(0);
        String ws = (String)input.get(1);
        String user = (String)input.get(2);
        String message = (String)input.get(3);

        if (!eventsHolder.isEventExists(event)) {
            return EVENTS_DOESNT_EXIST;
        }

        StringBuilder validated = new StringBuilder();
        Map<String, Object> values = eventsHolder.getParametersValues(event, message);

        for (Parameter param : eventsHolder.getDefinition(event).getParameters().getParams()) {
            String name = param.getName();
            String value = values.containsKey(name) ? valueOf(values.get(name)) : "";

            switch (name) {
                case WS:
                    if (isEmptyOrDefaultValue(ws) && !param.isAllowEmptyValue()) {
                        append(validated, WORKSPACE_IS_EMPTY);
                    }
                    break;

                case USER:
                    if (isEmptyOrDefaultValue(user) && !param.isAllowEmptyValue()) {
                        append(validated, USER_IS_EMPTY);
                    }
                    break;

                case PAAS:
                case TYPE:
                    String[] allowedValues = param.getAllowedValues() != null ? param.getAllowedValues().split(",")
                                                                              : name.equals(PAAS) ? AbstractProjectPaas.PAASES
                                                                                                  : AbstractProjectType.TYPES;
                    if (isEmptyValue(value)) {
                        if (!isAllowedValue(allowedValues, value) && !param.isAllowEmptyValue()) {
                            append(validated, String.format(VALUE_IS_NOT_ALLOWED, value, name));
                        }
                    } else if (!isAllowedValue(allowedValues, value)) {
                        append(validated, String.format(VALUE_IS_NOT_ALLOWED, value, name));
                    }
                    break;

                default:
                    if ((isEmptyValue(value) && !param.isAllowEmptyValue()) ||
                        (!isEmptyValue(value) && !isAllowedValue(param.getAllowedValues(), value))) {
                        append(validated, String.format(VALUE_IS_NOT_ALLOWED, value, param.getName()));
                    }
                    break;
            }
        }

        return validated.length() > 0 ? validated.toString() : null;
    }

    private void append(StringBuilder validated, String str) {
        if (validated.length() > 0) {
            validated.append(", ");
        }
        validated.append(str);
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }

    private boolean isAllowedValue(String allowedValues, String value) {
        return allowedValues == null || isAllowedValue(allowedValues.split(","), value);
    }

    private boolean isAllowedValue(String[] allowedValues, String value) {
        Set<String> values = new HashSet<>(Arrays.asList(allowedValues));
        return value != null && values.contains(value.toLowerCase());
    }

    private boolean isEmptyOrDefaultValue(String value) {
        return isEmptyValue(value) || value.equalsIgnoreCase(DEFAULT);
    }

    private boolean isEmptyValue(String value) {
        return isNullValue(value) || value.isEmpty();
    }

    private boolean isNullValue(String value) {
        return value == null || value.equalsIgnoreCase(NULL);
    }
}
