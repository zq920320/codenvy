/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * <li>0 - the workspace name</li><br>
 * <li>1 - the user name</li><br>
 * <li>2 - the project name</li><br>
 * <li>3 - the project type</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsFilter extends AbstractFilter {

    private final static int USER         = 0;
    private final static int PROJECT_NAME = 2;
    private final static int PROJECT_TYPE = 3;

    public ProjectsFilter(ListListStringValueData valueData) {
        super(valueData);
    }

    /**
     * @return the unique projects only
     */
    public ListListStringValueData getUniqueProjects() {
        HashSet<String> keys = new HashSet<String>();
        List<ListStringValueData> result = new ArrayList<ListStringValueData>();

        for (ListStringValueData item : valueData.getAll()) {
            List<String> project = item.getAll();

            StringBuilder builder = new StringBuilder();
            builder.append(project.get(USER));
            builder.append("/");
            builder.append(project.get(PROJECT_NAME));
            builder.append("/");
            builder.append(project.get(PROJECT_TYPE));

            String key = builder.toString();
            if (!keys.contains(key)) {
                keys.add(key);
                result.add(item);
            }
        }

        return new ListListStringValueData(result);
    }

    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
            case FILTER_PROJECT_NAME:
            case FILTER_PROJECT_TYPE:
                return key.ordinal();
            default:
                throw new IllegalArgumentException();
        }
    }
}
