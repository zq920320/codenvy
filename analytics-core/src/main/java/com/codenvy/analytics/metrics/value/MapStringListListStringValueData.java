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


package com.codenvy.analytics.metrics.value;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapStringListListStringValueData extends MapValueData<String, ListListStringValueData> {

    public static final MapStringListListStringValueData DEFAULT = new MapStringListListStringValueData(new HashMap<String, ListListStringValueData>(0));

    public MapStringListListStringValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }
    public MapStringListListStringValueData(Map<String, ListListStringValueData> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ListListStringValueData unionValues(ListListStringValueData v1, ListListStringValueData v2) {
        return (ListListStringValueData)v1.union(v2);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Map<String, ListListStringValueData> value) {
        return new MapStringListListStringValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeKey(ObjectOutputStream out, String key) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(ObjectOutputStream out, ListListStringValueData value) throws IOException {
        value.writeTo(out);
    }

    private static Map<String, ListListStringValueData> readFrom(ObjectInputStream in) throws IOException {
        int size = in.readInt();
        Map<String, ListListStringValueData> result = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            result.put(in.readUTF(), new ListListStringValueData(in));
        }

        return result;
    }
}
