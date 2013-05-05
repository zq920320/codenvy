/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class MapValueData<K extends ValueData, V extends ValueData> extends AbstractValueData {

    protected final Map<K, V> value;

    public MapValueData(String value) {
        this.value = parse(value);
    }

    public MapValueData(Map<K, V> value) {
        this.value = new HashMap<K,V>(value.size());
        this.value.putAll(value);
    }

    /**
     * @return unmodifiable {@link #value}
     */
    public Map<K, V> getAll() {
        return Collections.unmodifiableMap(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (Entry<K, V> entry : value.entrySet()) {
            if (builder.length() != 0) {
                builder.append(ITEM_DELIMITER);
            }

            builder.append(' ');
            builder.append(entry.getKey().getAsString());
            builder.append(KEY_VALUE_DELIMITER);
            builder.append(entry.getValue().getAsString());
        }

        if (builder.length() != 0) {
            builder.setCharAt(0, '[');
            builder.append(']');
        } else {
            builder.append('[');
            builder.append(EMPTY_VALUE);
            builder.append(']');
        }

        return builder.toString();
    }

    protected Map<K, V> parse(String line) {
        line = line.substring(1, line.length() - 1); // removes '[' and ']'

        if (line.equals(EMPTY_VALUE)) {
            return Collections.<K, V> emptyMap();
        }

        String[] splittedLine = line.split(ITEM_DELIMITER);

        Map<K, V> result = new HashMap<K, V>(splittedLine.length);
        for (String str : splittedLine) {
            String[] entry = str.split(KEY_VALUE_DELIMITER);

            K key = createInnerValueDataForKey(entry[0].trim());
            V value = createInnerValueDataForValue(entry[1].trim());

            result.put(key, value);
        }

        return result;
    }

    /** @return K instance */
    abstract protected K createInnerValueDataForKey(String str);

    /** @return V instance */
    abstract protected V createInnerValueDataForValue(String str);


    /**
     * Return the union of two maps. If maps contain the same key the resulted value for this key will be union of those values.
     */
    @SuppressWarnings("unchecked")
    protected Map<K, V> unionInternalValues(ValueData valueData) {
        MapValueData<K, V> addValueData = (MapValueData<K, V>)valueData;

        Map<K, V> newValue = new HashMap<K, V>(this.value);

        for (Entry<K, V> entry : addValueData.value.entrySet()) {
            V newMapValue = entry.getValue();

            if (newValue.containsKey(entry.getKey())) {
                newMapValue = (V)newMapValue.union(newValue.get(entry.getKey()));
            }

            newValue.put(entry.getKey(), newMapValue);

        }

        return newValue;
    }

    /** {@inheritedDoc} */
    @Override
    protected boolean doEquals(Object object) {
        MapValueData< ? , ? > valueData = (MapValueData< ? , ? >)object;
        
        if (this.value.size() != valueData.value.size()) {
            return false;
        }
        
        for (Entry<K, V> entry : this.value.entrySet()) {
            if (!entry.getValue().equals(valueData.value.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Ensures the different item order will provide the same result.<br>
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        int hash = 0;

        for (Entry<K, V> entry : value.entrySet()) {
            hash += entry.getKey().hashCode() * entry.getValue().hashCode();
        }

        return hash;
    }
}
