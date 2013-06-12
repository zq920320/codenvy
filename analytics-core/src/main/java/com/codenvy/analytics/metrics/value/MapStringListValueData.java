/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MapStringListValueData extends MapValueData<String, ListStringValueData> {

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
