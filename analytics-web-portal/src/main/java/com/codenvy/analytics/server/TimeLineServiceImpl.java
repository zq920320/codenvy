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
import com.codenvy.analytics.metrics.MetricParameter;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineServiceImpl extends RemoteServiceServlet implements TimeLineService {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(TimeLineServiceImpl.class);
    private static final String  FILE_NAME_PREFIX = "timeline";
    private static final Display display          = Display.initialize("view/time-line.xml");

    /** {@inheritDoc} */
    public List<TableData> getData(TimeUnit timeUnit, Map<String, String> filterContext) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);

            if (!filterContext.isEmpty()) {
                if (filterContext.containsKey(MetricFilter.FILTER_COMPANY.name())) {
                    replaceCompanyFilter(filterContext);
                }

                context.putAll(filterContext);
                return display.retrieveData(context);
            } else {
                try {
                    return PersisterUtil.loadTablesFromBinFile(getBinFileName(timeUnit));
                } catch (IOException e) {
                    // let's calculate then
                }

                List<TableData> data = display.retrieveData(context);
                PersisterUtil.saveTablesToCsvFile(data, getCsvFileName(timeUnit));
                PersisterUtil.saveTablesToBinFile(data, getBinFileName(timeUnit));

                return data;
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void replaceCompanyFilter(Map<String, String> filterContext) throws IOException {
        String company = filterContext.get(MetricFilter.FILTER_COMPANY.name());

        Map<String, String> context = Utils.newContext();
        Utils.putToDateDefault(context);
        Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);
        context.put(MetricParameter.COMPANY_NAME.name(), company);

        StringBuilder builder = new StringBuilder();

        ListStringValueData valueData = (ListStringValueData) ScriptExecutor.INSTANCE.executeAndReturn(ScriptType.USERS_BY_COMPANY, context);
        for (String user : valueData.getAll()) {
            if (builder.length() != 0) {
                builder.append(",");
            }

            builder.append(user);
        }

        filterContext.remove(MetricFilter.FILTER_COMPANY.name());
        filterContext.put(MetricFilter.FILTER_USER.name(), builder.toString());
    }

    /** Calculates view for given {@link TimeUnit} and preserves data. */
    public void update(TimeUnit timeUnit) throws Exception {
        Map<String, String> context = Utils.initializeContext(timeUnit);
        List<TableData> tables = display.retrieveData(context);

        PersisterUtil.saveTablesToCsvFile(tables, getCsvFileName(timeUnit));
        PersisterUtil.saveTablesToBinFile(tables, getBinFileName(timeUnit));
    }

    private String getCsvFileName(TimeUnit timeUnit) {
        return FILE_NAME_PREFIX + "_" + timeUnit.name() + ".csv";
    }

    private String getBinFileName(TimeUnit timeUnit) {
        return FILE_NAME_PREFIX + "-" + timeUnit.name() + ".bin";
    }
}
