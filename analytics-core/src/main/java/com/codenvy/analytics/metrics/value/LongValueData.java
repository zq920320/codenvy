/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class LongValueData extends AbstractValueData {

    private static final long          serialVersionUID = 1L;
    public static final  LongValueData EMPTY            = new LongValueData(0);

    private long value;

    public LongValueData() {
    }

    public LongValueData(String value) {
        this.value = Long.valueOf(value);
    }

    public LongValueData(long value) {
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getAsString() {
        return Long.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    protected LongValueData doUnion(ValueData valueData) {
        return new LongValueData(value + valueData.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public long getAsLong() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public double getAsDouble() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doEquals(Object object) {
        return value == ((LongValueData)object).value;
    }

    /** {@inheritDoc} */
    @Override
    public int doHashCode() {
        return (int)value;
    }

    /** {@inheritedDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(value);

    }

    /** {@inheritedDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readLong();
    }
}
