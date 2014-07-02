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

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

/**
 * @author Dmytro Nochevnov
 */
public class CreateProjectId extends EvalFunc<String> {
    private static final String DELIMETER = "/";

    @Override
    public String exec(Tuple input) throws ExecException {
        if (input == null || input.size() < 3) {
            return null;
        }

        String user = (String)input.get(0);
        String ws = (String)input.get(1);
        String project = (String)input.get(2);

        return exec(user, ws, project);
    }

    public static String exec(String user, String ws, String project) {
        if (user == null || ws == null || project == null) {
            return null;
        }

        return user + DELIMETER + ws + DELIMETER + project;
    }

    @Override
    public Schema outputSchema(Schema input) {
        return new Schema(new Schema.FieldSchema(getSchemaName(this.getClass().getName().toLowerCase(), input), DataType.CHARARRAY));
    }
}
