/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.client.TimeLineViewService;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.layout.LayoutReader;
import com.codenvy.analytics.server.vew.layout.RowLayout;
import com.codenvy.analytics.server.vew.layout.ViewLayout;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineViewServiceImpl extends RemoteServiceServlet implements TimeLineViewService {

    private static final Logger     LOGGER                   = LoggerFactory.getLogger(QueryServiceImpl.class);
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
            Map<String, String> context = Utils.initilizeContext(timeUnit, date);

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
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
