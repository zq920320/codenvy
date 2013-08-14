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

import com.codenvy.analytics.client.FactoryUrlTimeLineService;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public abstract class AbstractService extends RemoteServiceServlet implements FactoryUrlTimeLineService {

    public List<TableData> getData(Map<String, String> context) throws Exception {
        Set<MetricFilter> filters = Utils.getAvailableFilters(context);

        if (filters.isEmpty()) {
            try {
                return PersisterUtil.loadTablesFromBinFile(getBinFileName(context));
            } catch (IOException e) {
                // let's calculate then
            }

            return calculateAndSave(context);
        } else {
            if (filters.contains(MetricFilter.FILTER_COMPANY)) {
                replaceCompanyFilter(context);
            }

            return getDisplay().retrieveData(context);
        }
    }

    public List<TableData> calculateAndSave(Map<String, String> context) throws Exception {
        List<TableData> data = getDisplay().retrieveData(context);
        PersisterUtil.saveTablesToCsvFile(data, getCsvFileName(context));
        PersisterUtil.saveTablesToBinFile(data, getBinFileName(context));

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
            putUnexistedUserEmail(context);
        }
    }

    private void putUnexistedUserEmail(Map<String, String> context) {
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

    protected abstract Display getDisplay();

    protected abstract String getBinFileName(Map<String, String> context);

    protected abstract String getCsvFileName(Map<String, String> context);
}
