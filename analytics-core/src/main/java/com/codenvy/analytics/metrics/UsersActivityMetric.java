/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersActivityMetric extends ReadBasedMetric {

    UsersActivityMetric() {
        super(MetricType.USER_ACTIVITY);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                MetricParameter.FROM_DATE,
                MetricParameter.TO_DATE,
                MetricParameter.ALIAS}));
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ListStringValueData valueData = (ListStringValueData)super.getValue(context);
        List<ListStringValueData> newValue = new ArrayList<>(valueData.size());

        for (String item : valueData.getAll()) {
            newValue.add(new ListStringValueData(MessageTransformer.transform(item)));
        }

        return new ListListStringValueData(newValue);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData createEmptyValueData() throws IOException {
        return ValueDataFactory.createEmptyValueData(ListStringValueData.class);
    }
}
