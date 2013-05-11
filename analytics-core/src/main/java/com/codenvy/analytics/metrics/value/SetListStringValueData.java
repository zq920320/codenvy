/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class SetListStringValueData extends SetValueData<ListStringValueData> {

    public SetListStringValueData() {
    }

    public SetListStringValueData(Collection<ListStringValueData> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeItem(ObjectOutput out, ListStringValueData item) throws IOException {
        out.writeObject(item);
    }

    /** {@inheritedDoc} */
    @Override
    protected ListStringValueData readItem(ObjectInput in) throws IOException {
        try {
            return (ListStringValueData)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(Collection<ListStringValueData> value) {
        return new SetListStringValueData(value);
    }
}
