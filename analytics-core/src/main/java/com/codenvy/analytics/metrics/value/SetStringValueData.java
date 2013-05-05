/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.Collection;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class SetStringValueData extends SetValueData<StringValueData> {

    public SetStringValueData(String value) {
        super(value);
    }

    public SetStringValueData(Collection<StringValueData> value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueData doUnion(ValueData valueData) {
        return new SetStringValueData(unionInternalValues(valueData));
    }

    /** {@inheritedDoc} */
    @Override
    protected StringValueData createInnerValueData(String str) {
        return new StringValueData(str);
    }
}
