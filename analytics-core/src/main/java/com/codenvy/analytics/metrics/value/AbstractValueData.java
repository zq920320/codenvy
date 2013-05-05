/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractValueData implements ValueData {

    protected static final String ITEM_DELIMITER      = ",";
    protected static final String KEY_VALUE_DELIMITER = "=";
    protected static final String EMPTY_VALUE         = "$EMPTY$";

    private Integer               hash;

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData union(ValueData valueData) {
        if (getClass() != valueData.getClass()) {
            throw new IllegalArgumentException("Can not union two different classes " + getClass().getName() + " and "
                                               + valueData.getClass().getName());
        }

        return doUnion(valueData);
    }

    /**
     * Combines two value data into one single instance.
     * 
     * @param valueData
     * @return new unmodifiable {@link ValueData}
     */
    protected ValueData doUnion(ValueData valueData) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        return getClass() == object.getClass() && doEquals(object);
    }

    /**
     * @see #equals(Object)
     */
    protected abstract boolean doEquals(Object object);

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = doHashCode();
        }

        return hash;
    }
    
    /**
     * @see #hashCode()
     */
    abstract protected int doHashCode();


    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAsLong() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getAsDouble() {
        throw new UnsupportedOperationException();
    }

}
