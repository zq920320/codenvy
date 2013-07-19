/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MapStringListListStringValueData extends MapValueData<String, ListListStringValueData> {

    private static final long serialVersionUID = 1L;

    public MapStringListListStringValueData() {
        super();
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
    protected void writeKey(ObjectOutput out, String key) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(ObjectOutput out, ListListStringValueData value) throws IOException {
        out.writeObject(value);
    }

    /** {@inheritDoc} */
    @Override
    protected String readKey(ObjectInput in) throws IOException {
        return in.readUTF();
    }

    /** {@inheritDoc} */
    @Override
    protected ListListStringValueData readValue(ObjectInput in) throws IOException, ClassNotFoundException {
        return (ListListStringValueData)in.readObject();
    }
}
