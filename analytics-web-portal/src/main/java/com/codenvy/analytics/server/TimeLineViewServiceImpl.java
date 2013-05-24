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

    private static final Logger     LOGGER                   = LoggerFactory.getLogger(TimeLineViewServiceImpl.class);
    private static final String     TIMELINE_DIR             = "timeline";
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
    public List<TimeLineViewData> getViews(TimeUnit timeUnit, Map<String, String> filters) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit, new Date());
            context.putAll(filters);

            if (filters.isEmpty()) {
                return loadTables(context);
            } else {
                return calculateTimelineView(context);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Calculates view for given {@link TimeUnit} and preserves data.
     */
    public void update(TimeUnit timeUnit) throws Exception {
        Map<String, String> context = Utils.initializeContext(timeUnit, new Date());
        saveTables(calculateTimelineView(context), timeUnit);
    }

    private List<TimeLineViewData> calculateTimelineView(Map<String, String> context) throws Exception {
        List<TimeLineViewData> result = new ArrayList<TimeLineViewData>();
        int length = Integer.valueOf(viewLayout.getAttributes().get(HISTORY_LENGTH_ATTRIBUTE)) + 1;

        for (List<RowLayout> rows : viewLayout.getLayout()) {
            TimeLineViewData data = new TimeLineViewData();

            for (RowLayout row : rows) {
                data.add(row.fill(context, length));
            }

            result.add(data);
        }

        return result;
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

    private List<TimeLineViewData> loadTables(Map<String, String> context) throws Exception {
        TimeUnit timeUnit = Utils.getTimeUnit(context);

        File file = getFile(timeUnit);
        if (!file.exists()) {
            List<TimeLineViewData> timelineView = calculateTimelineView(context);
            saveTables(timelineView, timeUnit);
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

    @SuppressWarnings("unchecked")
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
