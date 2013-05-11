/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListStringValueData extends ListValueData<String> {

    public ListStringValueData() {
    }

    public ListStringValueData(List<String> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(List<String> value) {
        return new ListStringValueData(value);
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
