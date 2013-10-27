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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapStringListValueData extends MapValueData<String, ListStringValueData> {

    public static final  MapStringListValueData DEFAULT          =
            new MapStringListValueData(new HashMap<String, ListStringValueData>(0));

    public MapStringListValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }

    public MapStringListValueData(Map<String, ListStringValueData> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ListStringValueData unionValues(ListStringValueData v1, ListStringValueData v2) {
        return (ListStringValueData)v1.union(v2);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Map<String, ListStringValueData> value) {
        return new MapStringListValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeKey(ObjectOutputStream out, String key) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(ObjectOutputStream out, ListStringValueData value) throws IOException {
        value.writeTo(out);
    }

    /** Deserialization. */
    private static Map<String, ListStringValueData> readFrom(ObjectInputStream in) throws IOException {
        int count = in.readInt();

        Map<String, ListStringValueData> result = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            result.put(in.readUTF(), new ListStringValueData(in));
        }

        return result;
    }
}
