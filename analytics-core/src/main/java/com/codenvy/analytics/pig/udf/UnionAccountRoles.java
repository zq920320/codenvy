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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.Utils.toArray;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static com.codenvy.analytics.metrics.AbstractMetric.ROLES;
import static java.util.Arrays.asList;

/**
 * @author Alexander Reshetnyak
 */
public class UnionAccountRoles extends EvalFunc<String> {

    private static final Metric METRIC = MetricFactory.getMetric(MetricType.USERS_ACCOUNTS_LIST);

    public UnionAccountRoles() {
    }

    /** {@inheritDoc} */
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 3) {
            return null;
        }

        String accountId = (String)input.get(0);
        String userId = (String)input.get(1);
        String newRoles = (String)input.get(2);

        Set<String> unionRoles = new LinkedHashSet<>(asList(toArray(newRoles)));
        unionRoles.addAll(getCutterRoles(accountId, userId));

        return unionRoles.toString();
    }

    private static Set<String> getCutterRoles(String accountId, String userId) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter._ID, UUIDFrom.exec(accountId + userId));


        ListValueData valueData = getAsList(METRIC, builder.build());
        if (valueData.isEmpty()) {
            return Collections.emptySet();
        } else {
            Map<String, ValueData> account = treatAsMap(valueData.getAll().get(0));
            return new LinkedHashSet<>(asList(toArray(account.get(ROLES).getAsString())));
        }
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}