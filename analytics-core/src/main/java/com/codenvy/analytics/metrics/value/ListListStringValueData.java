/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListListStringValueData extends ListValueData<ListStringValueData> {

    public ListListStringValueData(String value) {
        super(value);
    }

    public ListListStringValueData(List<ListStringValueData> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ListStringValueData createInnerValueData(String str) {
        return new ListStringValueData(str);
    }

    /** {@inheritedDoc} */
    @Override
    protected ListListStringValueData createValueData(List<ListStringValueData> value) {
        return new ListListStringValueData(value);
    }
}
