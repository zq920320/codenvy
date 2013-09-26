/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.pig;

import org.apache.pig.EvalFunc;
import org.apache.pig.PigWarning;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CutQueryParam extends EvalFunc<String> {

    /** {@inheritDoc} */
    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0) {
            return null;
        }

        try {
            String url = (String)input.get(0);
            String paramName = (String)input.get(1);

            if (url == null) {
                return null;
            } else if (paramName == null || paramName.isEmpty()) {
                return url;
            }

            return doCut(url, "&" + paramName + "=");
        } catch (ExecException e) {
            warn("Error reading input: " + e.getMessage(), PigWarning.UDF_WARNING_1);
            return null;
        }
    }

    public static String doCut(String url, String paramName) {
        int indexParam = url.indexOf(paramName);
        int nextIndexParam = url.indexOf("&", indexParam + 1);

        return indexParam == -1 ? url : url.substring(0, indexParam) +
                                        (nextIndexParam == -1 ? "" : url.substring(nextIndexParam, url.length()));
    }

    /** {@inheritDoc} */
    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input),
                                                 DataType.CHARARRAY));
    }

}
