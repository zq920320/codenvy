/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ListDoubleValueData extends ListValueData<Double> {

    public ListDoubleValueData() {
    }

    public ListDoubleValueData(List<Double> value) {
        super(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData createInstance(List<Double> value) {
        return new ListDoubleValueData(value);
    }

    /** {@inheritedDoc} */
    @Override
    protected void writeItem(ObjectOutput out, Double item) throws IOException {
        out.writeDouble(item);
    }

    /** {@inheritedDoc} */
    @Override
    protected Double readItem(ObjectInput in) throws IOException {
        return in.readDouble();
    }
}
