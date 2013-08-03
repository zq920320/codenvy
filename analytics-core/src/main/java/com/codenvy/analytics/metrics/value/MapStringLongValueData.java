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
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapStringLongValueData extends MapValueData<String, Long> {

    public static final MapStringLongValueData DEFAULT = new MapStringLongValueData(new HashMap<String, Long>(0));

    public MapStringLongValueData(ObjectInputStream in) throws IOException {
        super(readFrom(in));
    }

    public MapStringLongValueData(Map<String, Long> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected Long unionValues(Long v1, Long v2) {
        return v1 + v2;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Map<String, Long> value) {
        return new MapStringLongValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeKey(ObjectOutputStream out, String key) throws IOException {
        out.writeUTF(key);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeValue(ObjectOutputStream out, Long value) throws IOException {
        out.writeLong(value);
    }

    private static Map<String, Long> readFrom(ObjectInputStream in) throws IOException {
        int size = in.readInt();
        Map<String, Long> result = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            result.put(in.readUTF(), in.readLong());
        }

        return result;
    }
}
