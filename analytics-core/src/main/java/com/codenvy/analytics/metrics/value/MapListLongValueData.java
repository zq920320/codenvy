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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapListLongValueData extends MapValueData<ListStringValueData, Long> {

    public static final  MapListLongValueData DEFAULT          =
            new MapListLongValueData(new HashMap<ListStringValueData, Long>(0));
    private static final long                 serialVersionUID = 1L;

    public MapListLongValueData() {
        super();
    }

    public MapListLongValueData(Map<ListStringValueData, Long> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected Long unionValues(Long v1, Long v2) {
        return v1 + v2;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Map<ListStringValueData, Long> value) {
        return new MapListLongValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeKey(ObjectOutput out, ListStringValueData key) throws IOException {
        out.writeObject(key);
    }

    @Override
    protected void writeValue(ObjectOutput out, Long value) throws IOException {
        out.writeLong(value);
    }

    /** {@inheritDoc} */
    @Override
    protected ListStringValueData readKey(ObjectInput in) throws IOException, ClassNotFoundException {
        return (ListStringValueData)in.readObject();
    }

    /** {@inheritDoc} */
    @Override
    protected Long readValue(ObjectInput in) throws IOException, ClassNotFoundException {
        return in.readLong();
    }
}
