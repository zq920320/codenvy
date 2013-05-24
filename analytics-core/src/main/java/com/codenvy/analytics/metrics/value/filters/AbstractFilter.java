/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractFilter implements Filter {

    protected final ListListStringValueData valueData;

    public AbstractFilter(ListListStringValueData valueData) {
        this.valueData = valueData;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return valueData.size();
    }

    /** {@inheritDoc} */
    @Override
    public ListListStringValueData apply(MetricFilter key, String value) {
        try {
            return apply(getIndex(key), value);
        } catch (IllegalArgumentException e) {
            return ListListStringValueData.EMPYT;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Long> sizeOfGroups(MetricFilter key) {
        try {
            return sizeOfGroups(getIndex(key));
        } catch (IllegalArgumentException e) {
            return Collections.<String, Long> emptyMap();
        }
    }


    /** {@inheritDoc} */
    @Override
    public Set<String> getAvailable(MetricFilter key) {
        try {
            return getAvailable(getIndex(key));
        } catch (IllegalArgumentException e) {
            return Collections.<String> emptySet();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size(MetricFilter key, String value) {
        try {
            return size(getIndex(key), value);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    protected ListListStringValueData getUniqueActions(MetricFilter... filters) {
        HashSet<String> keys = new HashSet<String>();
        List<ListStringValueData> result = new ArrayList<ListStringValueData>();

        for (ListStringValueData item : valueData.getAll()) {
            List<String> all = item.getAll();

            StringBuilder builder = new StringBuilder();
            for (MetricFilter filter : filters) {
                builder.append(all.get(getIndex(filter)));
                builder.append(".");
            }

            String key = builder.toString();
            if (!keys.contains(key)) {
                keys.add(key);
                result.add(item);
            }
        }

        return new ListListStringValueData(result);
    }

    private int size(int index, String value) {
        int result = 0;

        for (ListStringValueData item : valueData.getAll()) {
            if (item.getAll().get(index).contains(value)) {
                result++;
            }
        }

        return result;
    }

    private Set<String> getAvailable(int index) {
        Set<String> result = new HashSet<String>();

        for (ListStringValueData item : valueData.getAll()) {
            result.add(item.getAll().get(index));
        }

        return result;
    }


    protected Map<String, Long> sizeOfGroups(int index) throws IllegalArgumentException {
        Map<String, Long> result = new HashMap<String, Long>();

        for (ListStringValueData item : valueData.getAll()) {
            String key = item.getAll().get(index);

            long prevValue = result.containsKey(key) ? result.get(key) : 0;
            result.put(key, prevValue + 1);
        }

        return result;
    }

    private ListListStringValueData apply(int index, String value) {
        List<ListStringValueData> list = new ArrayList<ListStringValueData>();

        for (ListStringValueData item : valueData.getAll()) {
            if (item.getAll().get(index).contains(value)) {
                list.add(item);
            }
        }

        return new ListListStringValueData(list);
    }

    /**
     * Returns filter index for the {@link #valueData}. Throws {@link IllegalArgumentException} if filter is not supported.
     */
    protected abstract int getIndex(MetricFilter key) throws IllegalArgumentException;
}
