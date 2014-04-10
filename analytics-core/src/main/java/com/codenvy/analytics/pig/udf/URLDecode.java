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
package com.codenvy.analytics.pig.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class URLDecode extends EvalFunc<String> {

    @Override
    public String exec(Tuple input) throws IOException {
        if (input == null || input.size() == 0) {
            return null;
        }

        String str = (String)input.get(0);
        try {
            return (str == null) ? null : decodeURL(str);
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            return str;
        }
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input),
                                                 DataType.CHARARRAY));
    }

    /**
     * Decode all characters in url, but except '+'.
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String decodeURL(String url) throws UnsupportedEncodingException {
        StringBuffer result = new StringBuffer(200);
        String decodedUrl = URLDecoder.decode(url, "UTF-8");

        int i=0;
        int j=0;

        while(j<decodedUrl.length()) {
            String srcChar = url.substring(i, i+1);
            String destChar = decodedUrl.substring(j, j+1);

            if (srcChar.startsWith("%")) {
                srcChar = url.substring(i, i+3);
                i+=2;
                srcChar = URLDecoder.decode(srcChar, "UTF-8");
            }

            if (!destChar.equals(srcChar) && "+".equals(srcChar)) {
                result.append(srcChar);
            } else {
                result.append(destChar);
            }
            i++;
            j++;
        }

        return result.toString();
    }
}
