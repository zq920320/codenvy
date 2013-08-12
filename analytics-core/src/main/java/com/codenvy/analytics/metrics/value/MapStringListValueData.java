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
public class MapStringListValueData extends MapValueData<String, ListStringValueData> {

    public static final  MapStringListValueData DEFAULT          =
            new MapStringListValueData(new HashMap<String, ListStringValueData>(0));
    private static final long                   serialVersionUID = 1L;

    public MapStringListValueData() {
        super();
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
    protected void writeKey(ObjectOutput out, String key) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(ObjectOutput out, ListStringValueData value) throws IOException {
        out.writeObject(value);
    }

    /** {@inheritDoc} */
    @Override
    protected String readKey(ObjectInput in) throws IOException {
        return in.readUTF();
    }

    /** {@inheritDoc} */
    @Override
    protected ListStringValueData readValue(ObjectInput in) throws IOException, ClassNotFoundException {
        return (ListStringValueData)in.readObject();
    }
}
