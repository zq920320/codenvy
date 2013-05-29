/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.AnalysisViewService;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.server.vew.layout.ViewLayout;
import com.codenvy.analytics.shared.TimeLineViewData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class AnalysisViewServiceImpl extends PersisteableService implements AnalysisViewService {

    private static final Logger     LOGGER        = LoggerFactory.getLogger(AnalysisViewServiceImpl.class);
    private static final String     VIEW_ANALYSIS = "view/analysis.xml";
    private static final String     ANALYSIS_FILE = "analysis.bin";

    /**
     * {@link ViewLayout} instance.
     */
    private static final ViewLayout viewLayout    = ViewLayout.initialize(VIEW_ANALYSIS);


    /** {@inheritDoc} */
    @Override
    public List<TimeLineViewData> getData() {
        try {
            return loadTablesFromFile(getFile());
        } catch (IOException e1) {
            // let's calculate
        }

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.getName(), MetricParameter.FROM_DATE.getDefaultValue());
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());
        Utils.putTimeUnit(context, TimeUnit.DAY);

        try {
            List<TimeLineViewData> data = viewLayout.retrieveData(context, 2);
            saveTablesToFile(data, getFile());

            return data;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void update() throws IOException {
        saveTablesToFile(getData(), getFile());
    }

    private File getFile() {
        return new File(FSValueDataManager.RESULT_DIRECTORY, ANALYSIS_FILE);
    }
}
