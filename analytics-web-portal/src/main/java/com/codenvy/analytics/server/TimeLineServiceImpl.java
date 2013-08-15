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

import com.codenvy.analytics.client.TimeLineService;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineServiceImpl extends RemoteServiceServlet implements TimeLineService {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(TimeLineServiceImpl.class);
    private static final Display DISPLAY          = Display.initialize("view/time-line.xml");
    private static final String  FILE_NAME_PREFIX = "timeline";

    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filter) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);

            if (filter.isEmpty()) {
                try {
                    return PersisterUtil.loadTablesFromBinFile(getFileName(context) + PersisterUtil.BIN_EXT);
                } catch (FileNotFoundException e) {
                    // let's calculate then
                }

                return calculateAndSave(context);
            } else {
                context.putAll(filter);
                return doFilter(context);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<TableData> doFilter(Map<String, String> context) throws Exception {
        if (context.containsKey(MetricFilter.FILTER_COMPANY.name())) {
            replaceCompanyFilter(context);
        }

        return DISPLAY.retrieveData(context);
    }

    /** Updates time-line fully. */
    public void update() {
        try {
            calculateAndSave(Utils.initializeContext(TimeUnit.DAY));
            calculateAndSave(Utils.initializeContext(TimeUnit.WEEK));
            calculateAndSave(Utils.initializeContext(TimeUnit.MONTH));
            calculateAndSave(Utils.initializeContext(TimeUnit.LIFETIME));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private List<TableData> calculateAndSave(Map<String, String> context) throws Exception {
        List<TableData> data = DISPLAY.retrieveData(context);
        PersisterUtil.saveTablesToCsvFile(data, getFileName(context) + PersisterUtil.CSV_EXT);
        PersisterUtil.saveTablesToBinFile(data, getFileName(context) + PersisterUtil.BIN_EXT);

        return data;
    }

    private void replaceCompanyFilter(Map<String, String> context) throws IOException {
        String company = context.get(MetricFilter.FILTER_COMPANY.name());
        List<String> users = getUsersByCompany(company);

        context.remove(MetricFilter.FILTER_COMPANY.name());

        if (!users.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String user : users) {
                if (builder.length() != 0) {
                    builder.append(",");
                }

                builder.append(user);
            }
            context.put(MetricFilter.FILTER_USER.name(), builder.toString());
        } else {
            putUnExistedUserEmail(context);
        }
    }

    private void putUnExistedUserEmail(Map<String, String> context) {
        context.put(MetricFilter.FILTER_USER.name(), "_@@");
    }

    private List<String> getUsersByCompany(String company) throws IOException {
        Map<String, String> context = Utils.newContext();
        Utils.putToDateDefault(context);
        Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);
        Utils.putParam(context, company);

        ListStringValueData valueData =
                (ListStringValueData)ScriptExecutor.INSTANCE.executeAndReturn(ScriptType.USERS_BY_COMPANY, context);

        return valueData.getAll();
    }

    /** @return corresponding file name */
    private String getFileName(Map<String, String> context) {
        TimeUnit timeUnit = Utils.getTimeUnit(context);
        return FILE_NAME_PREFIX + "_" + timeUnit.name();
    }
}
