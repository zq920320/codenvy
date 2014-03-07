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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductUsageSessionsList extends AbstractListValueResulted {

    private static final String EMPTY_SESSION_MARKER = "User Did Not Enter Workspace";

    public ProductUsageSessionsList() {
        super(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            USER,
                            USER_COMPANY,
                            DOMAIN,
                            TIME,
                            END_TIME,
                            SESSION_ID,
                            DATE,
                            LOGOUT_INTERVAL};
    }

    @Override
    protected ValueData postEvaluation(ValueData valueData, Map<String, String> clauses) throws IOException {
        List<ValueData> value = new ArrayList<>();
        ListValueData list2Return = (ListValueData)valueData;

        for (ValueData items : list2Return.getAll()) {
            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());

            LongValueData date = (LongValueData)items2Return.get(DATE);
            LongValueData delta = (LongValueData)items2Return.get(TIME);
            String sessionId = items2Return.get(SESSION_ID).getAsString();

            if (sessionId == null || sessionId.isEmpty()) {
                items2Return.put(SESSION_ID, StringValueData.valueOf(EMPTY_SESSION_MARKER));
            }
            items2Return.put(START_TIME, date);
            items2Return.put(END_TIME, LongValueData.valueOf(date.getAsLong() + delta.getAsLong()));

            value.add(new MapValueData(items2Return));
        }

        return new ListValueData(value);
    }

    @Override
    public String getDescription() {
        return "Users' sessions";
    }
}
