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
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
public class ProjectsList extends AbstractListValueResulted {

    private static final String OTHER_NULL    = "null";
    private static final String OTHER_DEFAULT = "default";

    public ProjectsList() {
        super(MetricType.PROJECTS_LIST);
    }

    @Override
    public String getDescription() {
        return "Users' projects data";
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PROJECTS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            USER,
                            WS,
                            PROJECT,
                            PROJECT_TYPE
        };
    }

    @Override
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        List<ValueData> list2Return = new ArrayList<>();

        for (ValueData row : ((ListValueData)valueData).getAll()) {
            MapValueData prevItems = (MapValueData)row;
            Map<String, ValueData> items2Return = new HashMap<>(prevItems.getAll());


            for (Map.Entry<String, ValueData> entry : prevItems.getAll().entrySet()) {
                String value = entry.getValue().getAsString();

                if (value.equalsIgnoreCase(OTHER_DEFAULT)
                    || value.equalsIgnoreCase(OTHER_NULL)) {
                    items2Return.put(entry.getKey(), StringValueData.DEFAULT);
                }
            }
            list2Return.add(new MapValueData(items2Return));
        }

        return new ListValueData(list2Return);
    }
}