/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.SingupAnalysisViewService;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.layout.ViewLayout;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class SingupAnalysisViewServiceImpl extends RemoteServiceServlet implements SingupAnalysisViewService {

    private static final Logger     LOGGER               = LoggerFactory.getLogger(SingupAnalysisViewServiceImpl.class);
    private static final String     VIEW_SINGUP_ANALYSIS = "view/singup-analysis.xml";

    /**
     * {@link ViewLayout} instance.
     */
    private static final ViewLayout viewLayout           = ViewLayout.initialize(VIEW_SINGUP_ANALYSIS);


    /** {@inheritDoc} */
    @Override
    public List<TimeLineViewData> getData() {
        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.FROM_DATE.getName(), MetricParameter.FROM_DATE.getDefaultValue());
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());

        try {
            return viewLayout.retrieveData(context, 2);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
