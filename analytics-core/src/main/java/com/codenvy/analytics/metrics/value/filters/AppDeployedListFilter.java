/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import java.util.ArrayList;

/**
 * Items in the list meet the requirements: <br>
 * <li>workspace name</li><br>
 * <li>user name</li><br>
 * <li>project name</li><br>
 * <li>project type</li><br>
 * <li>paas</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AppDeployedListFilter implements ValueDataFilter {
    private final int                     WORKSPACE = 0;
    private final int                     USER      = 1;
    private final int                     PROJECT   = 2;
    private final int                     TYPE      = 3;
    private final int                     PAAS      = 4;

    /**
     * List of all deployed projects.
     */
    private final ListListStringValueData valueData;

    public AppDeployedListFilter(ListListStringValueData valueData) {
        this.valueData = valueData;
    }

    /** {@inheritDoc} */
    @Override
    public ListListStringValueData doFilter(String key, String value) {
        if (key.equals(Metric.USER_FILTER_PARAM)) {
            return doFilter(USER, value);
        } else if (key.equals(Metric.WS_FILTER_PARAM)) {
            return doFilter(WORKSPACE, value);
        } else if (key.equals(Metric.PROJECT_FILTER_PARAM)) {
            return doFilter(PROJECT, value);
        } else if (key.equals(Metric.TYPE_FILTER_PARAM)) {
            return doFilter(TYPE, value);
        } else if (key.equals(Metric.PAAS_FILTER_PARAM)) {
            return doFilter(PAAS, value);
        }

        return valueData;
    }

    private ListListStringValueData doFilter(int index, String value) {
        ArrayList<ListStringValueData> result = new ArrayList<ListStringValueData>();

        for (ListStringValueData listVD : valueData.getAll()) {
            if (listVD.getAll().get(index).getAsString().equals(value)) {
                result.add(listVD);
            }
        }

        return new ListListStringValueData(result);
    }
}
