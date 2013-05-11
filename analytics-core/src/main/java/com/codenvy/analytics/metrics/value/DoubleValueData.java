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
public class DoubleValueData extends AbstractValueData {

    private double value;

    public DoubleValueData() {
    }

    public DoubleValueData(double value) {
        this.value = value;
    }

    public DoubleValueData(long value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString() {
        return Double.toString(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DoubleValueData doUnion(ValueData valueData) {
        return new DoubleValueData(value + valueData.getAsDouble());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAsDouble() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doEquals(Object object) {
        return value == ((DoubleValueData)object).value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        return (int)value;
    }

    /** {@inheritedDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(value);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readDouble();
    }
}