/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Items in the list meet the requirements: <br>
 * <li>workspace name</li> <li>user name</li> <li>project name</li> <li>project type</li>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedListFilter implements ValueDataFilter {

    private final int                     WORKSPACE = 0;
    private final int                     USER      = 1;
    private final int                     PROJECT   = 2;
    private final int                     TYPE      = 3;

    private final ListListStringValueData valueData;

    public ProjectCreatedListFilter(ListListStringValueData valueData) {

        this.valueData = valueData;
    }

    public SetStringValueData getAllUsers() {
        Set<StringValueData> result = new HashSet<StringValueData>();
        for (ListStringValueData value : valueData.getAll()) {
            result.add(value.getAll().get(USER));
        }

        return new SetStringValueData(result);
    }

    public DoubleValueData getProjectsNumberByType(String type) {
        List<ListStringValueData> all = doFilter(Metric.TYPE_FILTER_PARAM, type).getAll();
        return new DoubleValueData(all.size());
    }

    public DoubleValueData getProjectsPercentByType(String type) {
        double projectsNumberByType = getProjectsNumberByType(type).getAsDouble();
        long totalNumber = valueData.getAll().size();

        return new DoubleValueData(100D * projectsNumberByType / totalNumber);
    }

    /**
     * @return distribution number of the projects by type
     */
    public MapStringLongValueData getProjectsNumberByTypes() {
        return getProjectsNumber(TYPE);
    }

    /**
     * @return distribution number of the projects by user
     */
    public MapStringLongValueData getProjectsNumberByUsers() {
        return getProjectsNumber(USER);
    }

    private MapStringLongValueData getProjectsNumber(int index) {
        Map<StringValueData, LongValueData> result = new HashMap<StringValueData, LongValueData>();

        for (ListStringValueData listVD : valueData.getAll()) {
            StringValueData key = listVD.getAll().get(index);

            long prevValue = result.containsKey(key) ? result.get(key).getAsLong() : 0;
            result.put(key, new LongValueData(prevValue + 1));
        }

        return new MapStringLongValueData(result);
    }


    private ListListStringValueData doFilter(int index, String item) {
        List<ListStringValueData> result = new ArrayList<ListStringValueData>();

        StringValueData itemVD = new StringValueData(item);

        for (ListStringValueData listVD : valueData.getAll()) {
            if (listVD.getAll().get(index).equals(itemVD)) {
                result.add(listVD);
            }
        }

        return new ListListStringValueData(result);
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
        }

        return valueData;
    }
}
