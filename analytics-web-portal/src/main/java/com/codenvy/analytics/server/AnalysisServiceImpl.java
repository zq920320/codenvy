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


package com.codenvy.analytics.server;

import com.codenvy.analytics.client.AnalysisService;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
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
            return PersisterUtil.loadTablesFromBinFile(FILE_NAME);
        } catch (IOException e) {
            // let's calculate then
        }

        try {
            List<TableData> data = retrieveData();

            for (TableData table : data) {
                PersisterUtil.saveTableToCsvFile(table, table.getCsvFileName());
            }
            PersisterUtil.saveTablesToBinFile(data, FILE_NAME);

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
        return context;
    }

    public void update() throws Exception {
        List<TableData> data = retrieveData();

        for (TableData table : data) {
            PersisterUtil.saveTableToCsvFile(table, table.getCsvFileName());
        }
        PersisterUtil.saveTablesToBinFile(data, FILE_NAME);
    }

    private List<TableData> retrieveData() throws Exception {
        Map<String, String> context = getContext();
        return display.retrieveData(context);
    }
}
