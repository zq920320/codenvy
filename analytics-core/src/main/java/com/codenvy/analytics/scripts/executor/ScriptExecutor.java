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


package com.codenvy.analytics.scripts.executor;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface ScriptExecutor {

    public static final ScriptExecutor INSTANCE = new PigScriptExecutor();

    /**
     * Run the script and returns the result.
     *
     * @param scriptType
     *         specific script type to execute
     * @param context
     *         contains all necessary value parameters required but given {@link ScriptType}
     * @throws IOException
     *         if something gone wrong or if a required parameter is absent
     */
    ValueData executeAndReturn(ScriptType scriptType, Map<String, String> context) throws IOException;

}