/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListStringValueData extends ListValueData<StringValueData> {

    public ListStringValueData(String value) {
        super(value);
    }

    public ListStringValueData(List<StringValueData> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected StringValueData createInnerValueData(String str) {
        return new StringValueData(str);
    }

    /** {@inheritedDoc} */
    @Override
    protected ListStringValueData createValueData(List<StringValueData> value) {
        return new ListStringValueData(value);
    }
}
