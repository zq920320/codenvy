/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class StringValueData extends AbstractValueData {

    /**
     * Internal value.
     */
    private String value;

    public StringValueData() {
    }

    public StringValueData(String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doEquals(Object object) {
        return value.equals(((StringValueData)object).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        return value.hashCode();
    }

    /** {@inheritedDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(value);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readUTF();
    }
}