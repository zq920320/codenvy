/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;



/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class StringValueData extends AbstractValueData {

    /**
     * Internal value.
     */
    private final String value;

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
}