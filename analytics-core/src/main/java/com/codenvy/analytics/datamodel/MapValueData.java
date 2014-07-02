/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.datamodel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.codenvy.analytics.datamodel.ValueDataUtil.isDefault;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapValueData extends CollectionValueData {

    public static final MapValueData DEFAULT = new MapValueData(Collections.<String, ValueData>emptyMap());

    protected Map<String, ValueData> value;

    /** For serialization one. */
    public MapValueData() {
    }

    public MapValueData(Map<String, ValueData> value) {
        this.value = new LinkedHashMap<>(value);
    }

    public static MapValueData valueOf(Map<String, ValueData> value) {
        return new MapValueData(value);
    }

    public static MapValueData valueOf(String values) {
        Map<String, ValueData> value = new HashMap<>();
        for (String s : values.split(",")) {
            String[] pair = s.split("=");
            value.put(pair[0], StringValueData.valueOf(pair[1]));
        }

        return new MapValueData(value);
    }

    /** @return unmodifiable {@link #value} */
    public Map<String, ValueData> getAll() {
        return Collections.unmodifiableMap(value);
    }

    public int size() {
        return value.size();
    }

    @Override
    /**
     * @return map in json format like '{"id":"5624124","name":"wkvbjix@tkfbwrx.tkq"}'
     */
    public String getAsString() {
        StringBuilder builder = new StringBuilder();

        for (Entry<String, ValueData> entry : value.entrySet()) {
            if (builder.length() != 0) {
                builder.append(',');
            }

            builder.append('"');
            builder.append(entry.getKey());
            builder.append('"');
            
            builder.append(':');
            
            builder.append('"');
            builder.append(entry.getValue().getAsString());
            builder.append('"');
        }

        if (builder.length() != 0) {
            builder.insert(0, '{');
            builder.append('}');
        } else {
            builder.append("{}");
        }

        return builder.toString();
    }

    @Override
    public String getType() {
        return ValueDataTypes.MAP.toString();
    }

    @Override
    protected ValueData doSubtract(ValueData valueData) {
        Map<String, ValueData> result = new HashMap<>(value);

        for (Entry<String, ValueData> entry : treatAsMap(valueData).entrySet()) {
            String key = entry.getKey();

            if (result.containsKey(key)) {
                ValueData substracted = result.get(key).subtract(entry.getValue());

                if (isDefault(substracted)) {
                    result.remove(key);
                } else {
                    result.put(key, substracted);
                }
            }
        }

        return new MapValueData(result);
    }

    @Override
    protected ValueData doAdd(ValueData valueData) {
        Map<String, ValueData> result = new HashMap<>(value);

        for (Entry<String, ValueData> entry : treatAsMap(valueData).entrySet()) {
            String key = entry.getKey();

            if (result.containsKey(key)) {
                result.put(key, result.get(key).add(entry.getValue()));
            } else {
                result.put(key, entry.getValue());
            }
        }

        return new MapValueData(result);
    }

    @Override
    protected boolean doEquals(ValueData valueData) {
        return value.equals(((MapValueData)valueData).value);
    }

    @Override
    public int doHashCode() {
        return value.hashCode();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value.size());
        for (Entry<String, ValueData> entry : value.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();

        value = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            value.put(in.readUTF(), (ValueData)in.readObject());
        }
    }
}
