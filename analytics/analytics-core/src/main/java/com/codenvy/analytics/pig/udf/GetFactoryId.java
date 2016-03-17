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
import com.codenvy.analytics.metrics.Parameters;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/**
 * @author Alexander Reshetnyak
 */
public class GetFactoryId extends EvalFunc<String> {

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 1) {
            return null;
        }

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.WS_ID, (String)input.get(0));
        builder.put(Parameters.SORT, "-date");
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, 10);

        ListValueData data = getAsList(MetricFactory.getMetric(MetricType.FACTORIES_ACCEPTED_LIST), builder.build());
        if (data.size() == 0) {
            return null;
        }

        Map<String, ValueData> profile = treatAsMap(data.getAll().get(0));
        if (profile.containsKey(AbstractMetric.FACTORY_ID)) {
            String s = profile.get(AbstractMetric.FACTORY_ID).getAsString();
            if (s.trim().isEmpty()) {
                return null;
            }
            return s;
        } else {
            return null;
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}