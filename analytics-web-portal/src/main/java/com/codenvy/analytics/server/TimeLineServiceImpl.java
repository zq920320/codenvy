/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.TimeLineService;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineServiceImpl extends RemoteServiceServlet implements TimeLineService {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(TimeLineServiceImpl.class);
    private static final String  FILE_NAME_PREFIX = "timeline";
    private static final Display display          = Display.initialize("view/time-line.xml");

    /**
     * {@inheritDoc}
     */
    public List<TableData> getData(TimeUnit timeUnit, String userFilter) {
        try {
            Map<String, String> context = Utils.initializeContext(timeUnit);

            if (!userFilter.isEmpty()) {
                context.put(MetricFilter.FILTER_USER.name(), "*" + userFilter + "*");
                return display.retrieveData(context);
            } else {
                try {
                    return PersisterUtil.loadTablesFromFile(getFileName(timeUnit));
                } catch (IOException e) {
                    // let's calculate then
                }

                List<TableData> data = display.retrieveData(context);
                PersisterUtil.saveTablesToFile(data, getFileName(timeUnit));

                return data;
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
        Map<String, String> context = Utils.initializeContext(timeUnit);
        PersisterUtil.saveTablesToFile(display.retrieveData(context), getFileName(timeUnit));
    }

    private String getFileName(TimeUnit timeUnit) {
        return FILE_NAME_PREFIX + "-" + timeUnit.name() + ".bin";
    }
}
