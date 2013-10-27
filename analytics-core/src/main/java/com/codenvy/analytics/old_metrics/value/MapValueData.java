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


package com.codenvy.analytics.old_metrics.value;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class MapValueData<K, V> extends AbstractValueData implements CollectionableValueData {

    protected Map<K, V> value;

    public MapValueData(Map<K, V> value) {
        this.value = new HashMap<>(value.size());
        this.value.putAll(value);
    }

    /** @return unmodifiable {@link #value} */
    public Map<K, V> getAll() {
        return Collections.unmodifiableMap(value);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return value.size();
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (Entry<K, V> entry : value.entrySet()) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append(' ');
            builder.append(entry.getKey().toString());
            builder.append('=');
            builder.append(entry.getValue().toString());
        }

        if (builder.length() != 0) {
            builder.setCharAt(0, '[');
            builder.append(']');
        } else {
            builder.append("[]");
        }

        return builder.toString();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected ValueData doUnion(ValueData valueData) {
        MapValueData<K, V> addVD = (MapValueData<K, V>)valueData;

        Map<K, V> result = new HashMap<>(this.value);

        for (Entry<K, V> entry : addVD.value.entrySet()) {
            K key = entry.getKey();

            if (result.containsKey(key)) {
                result.put(key, unionValues(result.get(key), entry.getValue()));
            } else {
                result.put(key, entry.getValue());
            }
        }

        return createInstance(result);
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(ObjectOutputStream out) throws IOException {
        out.writeInt(value.size());
        for (Entry<K, V> entry : value.entrySet()) {
            writeKey(out, entry.getKey());
            writeValue(out, entry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(Object object) {
        MapValueData<?, ?> valueData = (MapValueData<?, ?>)object;

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

    /** {@inheritDoc} */
    @Override
    public int doHashCode() {
        int hash = 0;

        for (Entry<K, V> entry : value.entrySet()) {
            hash += entry.getKey().hashCode() * entry.getValue().hashCode();
        }

        return hash;
    }

    protected abstract V unionValues(V v1, V v2);

    protected abstract ValueData createInstance(Map<K, V> value);

    protected abstract void writeKey(ObjectOutputStream out, K key) throws IOException;

    protected abstract void writeValue(ObjectOutputStream out, V value) throws IOException;
}
