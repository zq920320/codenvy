/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import java.util.HashMap;
import java.util.Map;


/** @author <a href="mailto:abazko@exoplatform.com">Anatoliy Bazko</a> */
public class BaseTest {

    /** Relative path to temporary files. */
    public static final String        BASE_DIR = "target";

    protected TupleFactory            tupleFactory;
    protected HashMap<String, String> uuid;

    @BeforeClass
    public void setUp() throws Exception {
        tupleFactory = TupleFactory.getInstance();

        uuid = new HashMap<String, String>();
        uuid.put(MetricParameter.FROM_DATE.getName(), "20130520");
        uuid.put(MetricParameter.TO_DATE.getName(), "20130520");
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

    protected void putToDate(Map<String, String> params, String toDate) {
        params.put(MetricParameter.TO_DATE.getName(), toDate);
    }

    protected void putFromDate(Map<String, String> params, String toDate) {
        params.put(MetricParameter.FROM_DATE.getName(), toDate);
    }
}
