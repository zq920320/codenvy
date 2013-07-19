/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractValueData implements ValueData {

    private static final long serialVersionUID = 1L;

    private Integer hash;

    /** {@inheritDoc} */
    @Override
    public ValueData union(ValueData valueData) {
        if (getClass() != valueData.getClass()) {
            throw new IllegalArgumentException("Can not union two different classes " + getClass().getName() + " and "
                                               + valueData.getClass().getName());
        }

        return doUnion(valueData);
    }

    /** {@inheritedDoc} */
    @Override
    public String toString() {
        return getAsString();
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

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return getClass() == object.getClass() && doEquals(object);
    }

    /** @see #equals(Object) */
    protected abstract boolean doEquals(Object object);

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = doHashCode();
        }

        return hash;
    }


    /** @see #hashCode() */
    abstract protected int doHashCode();


    /** {@inheritDoc} */
    @Override
    public long getAsLong() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public double getAsDouble() {
        throw new UnsupportedOperationException();
    }

}
