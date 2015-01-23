/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/** @author Anatoliy Bazko */
public class GetSessionStartTime extends EvalFunc<Long> {

    /** {@inheritDoc} */
    @Override
    public Long exec(Tuple input) throws IOException {
        if (input == null || input.size() != 1) {
            return null;
        }

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.SESSION_ID, (String)input.get(0));

        ListValueData data = getAsList(MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST), builder.build());
        if (data.isEmpty()) {
            return null;
        }

        Map<String, ValueData> m = treatAsMap(data.getAll().get(0));
        return treatAsLong(m.get(AbstractMetric.DATE));
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.LONG));
    }
}