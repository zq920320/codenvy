/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
import com.codenvy.analytics.metrics.*;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.analytics.Utils.isAnonymousUser;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ReplaceUserWithId extends EvalFunc<String> {

    public ReplaceUserWithId() {
    }

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 1) {
            return null;
        }

        return exec((String)input.get(0));
    }

    public static String exec(String user) throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        if (user == null) {
            return null;

        } else if (isAnonymousUser(user)) {
            return user;

        } else {
            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.ALIASES, user.toLowerCase());

            ListValueData valueData = getAsList(metric, builder.build());
            if (valueData.size() == 0) {
                return user;
            } else {
                Map<String, ValueData> profile = treatAsMap(valueData.getAll().get(0));
                return profile.get(AbstractMetric.ID).getAsString();
            }
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }

}
