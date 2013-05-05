/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;

/**
 * Items in the list meet the requirements: <br>
 * <li>workspace name</li> <li>user name</li> <li>project name</li> <li>project type</li>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class DetailsProjectCreatedWrapper implements ValueDataWrapper {

    private final int                    WORKSPACE = 0;
    private final int                    USER      = 1;
    private final int                    PROJECT   = 2;
    private final int                    TYPE      = 3;

    private final ListListStringValueData valueData;

    public DetailsProjectCreatedWrapper(ListListStringValueData valueData) {
        this.valueData = valueData;
    }

    public SetStringValueData getAllUsers() {
        Set<StringValueData> result = new HashSet<StringValueData>();
        for (ListStringValueData value : valueData.getAll()) {
            result.add(value.getAll().get(USER));
        }

        return new SetStringValueData(result);
    }

    public ListListStringValueData getProjectsByUser(String user) {
        return doFilter(user, USER);
    }

    public ListListStringValueData getProjectsByType(String type) {
        return doFilter(type, TYPE);
    }

    public DoubleValueData getProjectsNumberByType(String type) {
        List<ListStringValueData> all = getProjectsByType(type).getAll();
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
        Map<StringValueData, LongValueData> result = new HashMap<StringValueData, LongValueData>();

        for (ListStringValueData listVD : valueData.getAll()) {
            StringValueData type = listVD.getAll().get(TYPE);

            long prevValue = result.containsKey(type) ? result.get(type).getAsLong() : 0;
            result.put(type, new LongValueData(prevValue + 1));
        }

        return new MapStringLongValueData(result);
    }

    private ListListStringValueData doFilter(String item, int index) {
        List<ListStringValueData> result = new ArrayList<ListStringValueData>();

        StringValueData itemVD = new StringValueData(item);

        for (ListStringValueData listVD : valueData.getAll()) {
            if (listVD.getAll().get(index).equals(itemVD)) {
                result.add(listVD);
            }
        }

        return new ListListStringValueData(result);
    }
}
