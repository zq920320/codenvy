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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
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

    public static final String WS           = "ws";
    public static final String USER         = "user";
    public static final String DOMAIN       = "domain";
    public static final String USER_COMPANY = "user_company";
    public static final String TIME         = "time";
    public static final String START_TIME   = "start_time";
    public static final String END_TIME     = "end_time";
    public static final String SESSION_ID   = "session_id";

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
                            DATE};
    }

    @Override
    protected ValueData postEvaluation(ValueData valueData, Map<String,String> clauses) throws IOException {
        List<ValueData> value = new ArrayList<>();
        ListValueData listValueData = (ListValueData)valueData;

        for (ValueData items : listValueData.getAll()) {
            MapValueData prevItems = (MapValueData)items;
            Map<String, ValueData> newItems = new HashMap<>(prevItems.getAll());

            LongValueData date = (LongValueData)newItems.get(DATE);
            LongValueData delta = (LongValueData)newItems.get(TIME);

            newItems.put(START_TIME, date);
            newItems.put(END_TIME, LongValueData.valueOf(date.getAsLong() + delta.getAsLong()));

            value.add(new MapValueData(newItems));
        }

        return new ListValueData(value);
    }

    @Override
    public String getStorageCollectionName() {
        return super.getStorageCollectionName();
    }

    @Override
    public String getDescription() {
        return "Users' sessions";
    }
}
