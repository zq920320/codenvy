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
public class SetStringValueData extends SetValueData<String> {

    public SetStringValueData() {
    }

    public SetStringValueData(Collection<String> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(Collection<String> value) {
        return new SetStringValueData(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeItem(ObjectOutput out, String item) throws IOException {
        out.writeUTF(item);
    }

    /** {@inheritedDoc} */
    @Override
    protected String readItem(ObjectInput in) throws IOException {
        return in.readUTF();
    }
}
