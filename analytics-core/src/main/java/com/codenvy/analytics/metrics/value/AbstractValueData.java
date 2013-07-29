/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
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
