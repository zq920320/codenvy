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
public class MapStringFixedLongListValueData extends MapValueData<String, FixedListLongValueData> {

    public static final  MapStringFixedLongListValueData DEFAULT          =
            new MapStringFixedLongListValueData(new HashMap<String, FixedListLongValueData>(0));
    private static final long                            serialVersionUID = 1L;

    public MapStringFixedLongListValueData() {
        super();
    }

    public MapStringFixedLongListValueData(Map<String, FixedListLongValueData> value) {
        super(value);
    }

    /** {@inheritDoc} */
    @Override
    protected FixedListLongValueData unionValues(FixedListLongValueData v1, FixedListLongValueData v2) {
        return (FixedListLongValueData)v1.union(v2);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createInstance(Map<String, FixedListLongValueData> value) {
        return new MapStringFixedLongListValueData(value);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeKey(ObjectOutput out, String key) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(ObjectOutput out, FixedListLongValueData value) throws IOException {
        out.writeObject(value);
    }

    /** {@inheritDoc} */
    @Override
    protected String readKey(ObjectInput in) throws IOException {
        return in.readUTF();
    }

    /** {@inheritDoc} */
    @Override
    protected FixedListLongValueData readValue(ObjectInput in) throws IOException, ClassNotFoundException {
        return (FixedListLongValueData)in.readObject();
    }
}
