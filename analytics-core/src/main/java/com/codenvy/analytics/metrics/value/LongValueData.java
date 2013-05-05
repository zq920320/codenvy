/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class LongValueData extends AbstractValueData {

    private final Long value;

    public LongValueData(String value) {
        this.value = Long.valueOf(value);
    }

    public LongValueData(Long value) {
        this.value = value;
    }

    public LongValueData(int value) {
        this.value = Long.valueOf(value);
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
    protected LongValueData doUnion(ValueData valueData) {
        return new LongValueData(value + valueData.getAsLong());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAsLong() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getAsDouble() {
        return Double.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doEquals(Object object) {
        return value.equals(((LongValueData)object).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int doHashCode() {
        return value.intValue();
    }
}
