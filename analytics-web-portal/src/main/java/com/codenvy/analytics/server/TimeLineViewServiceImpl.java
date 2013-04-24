/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.server.view.TimeLineViewManager;

import com.codenvy.analytics.client.TimeLineViewService;
import com.codenvy.analytics.metrics.TimeIntervalUtil;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class TimeLineViewServiceImpl extends RemoteServiceServlet implements
                                                         TimeLineViewService {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLineViewServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    public List<TimeLineViewData> getViews(Date date, TimeUnit timeUnit) throws IOException {
        try {
            Map<String, String> context = initializeContext(date, timeUnit);

            TimeLineViewManager view = new TimeLineViewManager(TimeIntervalUtil.prevDateInterval(context));
            return view.getTimeLineViewData();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private Map<String, String> initializeContext(Date date, TimeUnit timeUnit) throws IOException, ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), timeUnit.toString());
        TimeIntervalUtil.initDateInterval(cal, context);

        return context;
    }
}
