/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class StringValueData extends AbstractValueData {

    private static final long serialVersionUID = 1L;

    private String value;

    public StringValueData() {
    }

    public StringValueData(String value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(value);
    }

    /** {@inheritDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readUTF();
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(Object object) {
        return value.equals(object);
    }

    /** {@inheritDoc} */
    @Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
