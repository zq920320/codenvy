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
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.analytics.Utils.isDefaultWorkspaceName;
import static com.codenvy.analytics.Utils.isWorkspaceID;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/**
 * @author Anatoliy Bazko
 */
public class ReplaceWsWithId extends EvalFunc<String> {

    private static final Metric METRIC = MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST);

    public ReplaceWsWithId() {
    }

    /** {@inheritDoc} */
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 2) {
            return null;
        }

        String ws = (String)input.get(0);
        String wsId = (String)input.get(1);

        return wsId != null ? wsId : doReplace(ws);
    }

    /** Does replacement. */
    public static String doReplace(String ws) throws IOException {
        if (ws == null || isDefaultWorkspaceName(ws) || isWorkspaceID(ws)) {
            return ws;

        } else {
            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.WS, ws.toLowerCase());

            ListValueData valueData = getAsList(METRIC, builder.build());
            if (valueData.size() == 0) {
                return ws;
            } else {
                Map<String, ValueData> profile = treatAsMap(valueData.getAll().get(0));
                return profile.get(AbstractMetric.ID).getAsString();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }

}
