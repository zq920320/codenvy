/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class DoubleValueData extends AbstractValueData {

    private final Double value;

    public DoubleValueData(String value) {
        this.value = Double.valueOf(value);
    }

    public DoubleValueData(Double value) {
        this.value = value;
    }

    public DoubleValueData(int value) {
        this.value = Double.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString() {
        return value.toString();
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
    public Double getAsDouble() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doEquals(Object object) {
        return value.equals(((DoubleValueData)object).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        return value.intValue();
    }
}