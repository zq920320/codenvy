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

import com.codenvy.analytics.Utils;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Reshetnyak
 */
public class ExtractParam extends EvalFunc<String> {

    private final static String PARAM_NAME    = "paramNameParam";
    private final static String PARAM_PATTERN = ".*\\sparamNameParam#([^\\s#][^#]*|)#.*";

    private final static Pattern parametersPattern = Pattern.compile(".*\\sPARAMETERS#([^\\s^#]+|)#.*");

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() != 2) {
            return null;
        }

        String message = (String)input.get(0);
        String paramNameParam = (String)input.get(1);

        if (!message.contains(paramNameParam) &&
            !message.contains(URLEncoder.encode(paramNameParam, "UTF-8"))) {
            return null;
        }

        Pattern pattern = Pattern.compile(PARAM_PATTERN.replaceAll(PARAM_NAME, paramNameParam));
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            matcher = parametersPattern.matcher(message);

            if (matcher.find()) {
                Map<String, String> params = Utils.fetchEncodedPairs(matcher.group(1), true);
                return params.get(paramNameParam.toLowerCase());
            }

            return null;
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input),
                                                 DataType.CHARARRAY));
    }
}
