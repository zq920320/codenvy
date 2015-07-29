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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static com.codenvy.analytics.metrics.AbstractMetric.ROLES;

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

        Set<String> unionRoles = rolesToSet(newRoles);

        unionRoles.addAll(getCutterRoles(accountId, userId));

        return unionRoles.toString().replaceAll(" ", "");
    }

    private static Set<String> getCutterRoles(String accountId, String userId) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter._ID, accountId + userId);


        ListValueData valueData = getAsList(METRIC, builder.build());
        if (valueData.isEmpty()) {
            return Collections.EMPTY_SET;
        } else {
            Map<String, ValueData> account = treatAsMap(valueData.getAll().get(0));
            return rolesToSet(account.get(ROLES).getAsString());
        }
    }

    public static Set<String> rolesToSet(String roles) throws IOException {
        String rolesNoSpace = roles.replaceAll(" ", "");
        return new LinkedHashSet<>(Arrays.asList(rolesNoSpace.substring(1, rolesNoSpace.length() - 1).split(",")));
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}