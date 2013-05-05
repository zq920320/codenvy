/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListDoubleValueData extends ListValueData<DoubleValueData> {

    public ListDoubleValueData(String value) {
        super(value);
    }

    public ListDoubleValueData(List<DoubleValueData> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected DoubleValueData createInnerValueData(String str) {
        return new DoubleValueData(str);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createValueData(List<DoubleValueData> value) {
        return new ListDoubleValueData(value);
    }
}
