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

package com.codenvy.analytics;

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import org.apache.pig.data.TupleFactory;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {

    /** Relative path to temporary files. */
    public static final String        BASE_DIR = "target";

    protected TupleFactory                  tupleFactory;
    protected LinkedHashMap<String, String> uuid;

    @BeforeClass
    public void setUp() throws Exception {
        tupleFactory = TupleFactory.getInstance();

        uuid = new LinkedHashMap<String, String>();
        uuid.put(MetricParameter.FROM_DATE.name(), "20130520");
        uuid.put(MetricParameter.TO_DATE.name(), "20130520");
    }

    protected ValueData executeAndReturnResult(ScriptType type, File log, Map<String, String> executionParams)
                                                                                                           throws IOException {
        executionParams.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        PigScriptExecutor scriptExecutor = new PigScriptExecutor();

        return scriptExecutor.executeAndReturn(type, executionParams);
    }

    protected void execute(ScriptType type, File log, Map<String, String> executionParams) throws IOException {
        executionParams.put(PigScriptExecutor.LOG, log.getAbsolutePath());
        PigScriptExecutor scriptExecutor = new PigScriptExecutor();
        scriptExecutor.execute(type, executionParams);
    }

    protected void prepareDir(String name) throws IOException {
        File dir = new File(name);
        dir.mkdirs();

        File file = new File(dir, "tmp");
        file.createNewFile();
    }
}
