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
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.toArray;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class GetDomain extends EvalFunc<String> {

    private static final Pattern EMAIL = Pattern.compile(".*@(.*)");

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 1) {
            return null;
        }

        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, (String)input.get(0));

        ListValueData data = getAsList(MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST), builder.build());
        if (data.size() == 0) {
            return null;
        }

        Map<String, ValueData> profile = treatAsMap(data.getAll().get(0));

        if (!profile.containsKey(AbstractMetric.ALIASES)) {
            return null;
        } else {
            String[] aliases = toArray(profile.get(AbstractMetric.ALIASES).getAsString());
            if (aliases.length == 0) {
                return null;
            } else {
                return EMAIL.matcher(aliases[0]).replaceFirst("$1");
            }
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}
