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

import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

    public List<TableData> getData(Map<String, String> context, Map<String, String> filter) throws Exception {
        if (filter.isEmpty()) {
            try {
                return PersisterUtil.loadTablesFromBinFile(getFileName(context) + PersisterUtil.BIN_EXT);
            } catch (FileNotFoundException e) {
                // let's calculate then
            }

            return update(context);
        } else {
            if (filter.size() > 1) {
                throw new IllegalStateException("Filter size > 1");
            }

            context.putAll(filter);
            return doFilter(context);
        }
    }

    public List<TableData> update(Map<String, String> context) throws Exception {
        List<TableData> data = calculate(context);
        save(data, context);

        return data;
    }

    /** Retries data. */
    private List<TableData> calculate(Map<String, String> context) throws Exception {
        List<TableData> data = new ArrayList<>();
        for (Display display : getDisplays()) {
            data.addAll(display.retrieveData(context));
        }

        return data;
    }

    /** Saves data to .csv and .bin files. */
    private void save(List<TableData> data, Map<String, String> context) throws IOException {
        PersisterUtil.saveTablesToBinFile(data, getFileName(context) + PersisterUtil.BIN_EXT);

        if (data.size() == 0 || data.get(0).getCsvFileName() == null) {
            PersisterUtil.saveTablesToCsvFile(data, getFileName(context) + PersisterUtil.CSV_EXT);
        } else {
            for (TableData table : data) {
                PersisterUtil.saveTableToCsvFile(table, table.getCsvFileName());
            }
        }
    }

    /** @return corresponding file name */
    protected abstract String getFileName(Map<String, String> context);

    /** @return all available displays */
    protected abstract Display[] getDisplays();

    /** @return filtered excerpt. */
    protected List<TableData> doFilter(Map<String, String> context) throws Exception {
        return new ArrayList<>(); // TODO
    }
}
