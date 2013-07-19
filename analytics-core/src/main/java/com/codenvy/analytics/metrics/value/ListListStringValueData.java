/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ListListStringValueData extends ListValueData<ListStringValueData> {

    private static final long serialVersionUID = 1L;

    public static final ListListStringValueData EMPTY =
            new ListListStringValueData(Collections.<ListStringValueData>emptyList());

    public ListListStringValueData() {
        super();
    }

    public ListListStringValueData(Collection<ListStringValueData> value) {
        super(value);
    }

    @Override
    protected ValueData createInstance(List<ListStringValueData> value) {
        return new ListListStringValueData(value);
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
}
