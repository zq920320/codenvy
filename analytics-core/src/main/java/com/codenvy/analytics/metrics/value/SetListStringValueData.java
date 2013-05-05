/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.Collection;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class SetListStringValueData extends SetValueData<ListStringValueData> {

    public SetListStringValueData(String value) {
        super(value);
    }

    public SetListStringValueData(Collection<ListStringValueData> value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueData doUnion(ValueData valueData) {
        return new SetListStringValueData(unionInternalValues(valueData));
    }

    /** {@inheritedDoc} */
    @Override
    protected ListStringValueData createInnerValueData(String str) {
        return new ListStringValueData(str);
    }
}
