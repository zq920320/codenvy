/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.AnalysisService;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPE;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class AnalysisServiceImpl extends RemoteServiceServlet implements AnalysisService {

    private static final Logger  LOGGER    = LoggerFactory.getLogger(AnalysisServiceImpl.class);
    private static final String  FILE_NAME = "analysis.bin";
    private static final Display display   = Display.initialize("view/analysis.xml");

    /** {@inheritDoc} */
    @Override
    public List<TableData> getData() {
        try {
            return PersisterUtil.loadTablesFromFile(FILE_NAME);
        } catch (IOException e) {
            // let's calculate then
        }

        try {
            List<TableData> data = retrieveData();
            PersisterUtil.saveTablesToFile(data, FILE_NAME);

            return data;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, String> getContext() {
        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.MONTH.name());
        context.put(MetricParameter.TO_DATE.name(), MetricParameter.TO_DATE.getDefaultValue());
        context.put(MetricParameter.FROM_DATE.name(), MetricParameter.FROM_DATE.getDefaultValue());
        context.put(MetricParameter.RESULT_DIR.name(), FSValueDataManager.RESULT_DIRECTORY);
        return context;
    }

    public void update() throws Exception {
        PersisterUtil.saveTablesToFile(retrieveData(), FILE_NAME);
    }

    private List<TableData> retrieveData() throws Exception {
        Map<String, String> context = getContext();

        for (ENTITY_TYPE eType : ENTITY_TYPE.values()) {
            File dir = new File(FSValueDataManager.RESULT_DIRECTORY, eType.name());
            FileUtils.deleteDirectory(dir);
        }

        FileUtils.deleteDirectory(new File(FSValueDataManager.RESULT_DIRECTORY, "LOG"));

        ScriptExecutor executor = ScriptExecutor.INSTANCE;
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_LOG_PREPARATION, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());

        context.put(MetricParameter.INTERVAL.name(), "P1D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P7D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P30D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P60D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P90D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P365D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.INTERVAL.name(), "P100Y");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_USERS, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());

        context.put(MetricParameter.INTERVAL.name(), "P1D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P7D");
        executor.executeAndReturn(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P30D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P60D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P90D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P365D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.INTERVAL.name(), "P100Y");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.COMPANIES.name());

        context.put(MetricParameter.INTERVAL.name(), "P1D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P7D");
        executor.executeAndReturn(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P30D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P60D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P90D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P365D");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.put(MetricParameter.INTERVAL.name(), "P100Y");
        executor.execute(ScriptType.PRODUCT_USAGE_TIME_COMPANIES, context);

        context.remove(MetricParameter.ENTITY.name());
        context.remove(MetricParameter.INTERVAL.name());

        return display.retrieveData(context);
    }
}
