/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.AnalysisService;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            List<TableData> data = display.retrieveData(getContext());
            PersisterUtil.saveTablesToFile(data, FILE_NAME);

            return data;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Map<String, String> getContext() {
        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());
        context.put(MetricParameter.FROM_DATE.getName(), MetricParameter.FROM_DATE.getDefaultValue());
        return context;
    }

    public void update() throws Exception {
        PersisterUtil.saveTablesToFile(display.retrieveData(getContext()), FILE_NAME);
    }

}
