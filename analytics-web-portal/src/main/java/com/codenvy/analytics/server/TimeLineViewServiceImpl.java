/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.TimeLineViewService;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.server.vew.layout.LayoutReader;
import com.codenvy.analytics.server.vew.layout.RowLayout;
import com.codenvy.analytics.server.vew.layout.ViewLayout;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineViewServiceImpl extends RemoteServiceServlet implements TimeLineViewService {

    private static final String     TIMELINE_DIR             = "timeline";
    private static final Logger     LOGGER                   = LoggerFactory.getLogger(TimeLineViewServiceImpl.class);
    private static final String     VIEW_TIME_LINE           = "view/time-line.xml";
    private static final String     HISTORY_LENGTH_ATTRIBUTE = "history-length";

    /**
     * Time line view layout.
     */
    private static final ViewLayout viewLayout;

    static {
        LayoutReader layout = new LayoutReader(VIEW_TIME_LINE);
        try {
            viewLayout = layout.read();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<TimeLineViewData> getViews(Date date, TimeUnit timeUnit) {
        try {
            return loadTables(date, timeUnit);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void updateTimelineView(Date date, TimeUnit timeUnit) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit, date);

            List<TimeLineViewData> result = new ArrayList<TimeLineViewData>();
            int length = Integer.valueOf(viewLayout.getAttributes().get(HISTORY_LENGTH_ATTRIBUTE)) + 1;

            for (List<RowLayout> rows : viewLayout.getLayout()) {
                TimeLineViewData data = new TimeLineViewData();

                for (RowLayout row : rows) {
                    data.add(row.fill(context, length));
                }

                result.add(data);
            }

            saveTables(result, timeUnit);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void saveTables(List<TimeLineViewData> tables, TimeUnit timeUnit) throws IOException {
        File file = getFile(timeUnit);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        saveTablesToFile(tables, file);
    }

    private List<TimeLineViewData> loadTables(Date date, TimeUnit timeUnit) throws IOException {
        File file = getFile(timeUnit);

        if (!file.exists()) {
            updateTimelineView(date, timeUnit);
        }

        return loadTablesFromFile(file);
    }

    private void saveTablesToFile(Object value, File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            out.writeObject(value);
        } finally {
            out.close();
        }
    }

    private List<TimeLineViewData> loadTablesFromFile(File file) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

        try {
            return (List<TimeLineViewData>)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
    }

    private File getFile(TimeUnit timeUnit) {
        return new File(FSValueDataManager.RESULT_DIRECTORY + File.separator + TIMELINE_DIR + File.separator
                        + timeUnit.toString());
    }

}
