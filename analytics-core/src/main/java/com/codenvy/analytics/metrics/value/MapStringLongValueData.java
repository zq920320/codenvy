/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MapStringLongValueData extends MapValueData<StringValueData, LongValueData> {

    public MapStringLongValueData(String value) {
        super(value);
    }

    public MapStringLongValueData(Map<StringValueData, LongValueData> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData doUnion(ValueData valueData) {
        return new MapStringLongValueData(unionInternalValues(valueData));
    }

    /** {@inheritedDoc} */
    @Override
    protected StringValueData createInnerValueDataForKey(String str) {
        return new StringValueData(str);
    }

    /** {@inheritedDoc} */
    @Override
    protected LongValueData createInnerValueDataForValue(String str) {
        return new LongValueData(str);
    }
}
