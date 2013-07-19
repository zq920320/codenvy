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
public class MapStringLongValueData extends MapValueData<String, Long> {

    private static final long serialVersionUID = 1L;

    public MapStringLongValueData() {
        super();
    }

    public MapStringLongValueData(Map<String, Long> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected Long unionValues(Long v1, Long v2) {
        return v1 + v2;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(Map<String, Long> value) {
        return new MapStringLongValueData(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeKey(ObjectOutput out, String key) throws IOException {
        out.writeUTF(key);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeValue(ObjectOutput out, Long value) throws IOException {
        out.writeLong(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected String readKey(ObjectInput in) throws IOException {
        return in.readUTF();
    }

    /** {@inheritedDoc} */
    @Override
    protected Long readValue(ObjectInput in) throws IOException {
        return in.readLong();
    }
}
