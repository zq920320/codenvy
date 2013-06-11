/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.client.UserService;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.vew.template.Display;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class UserServiceImpl extends RemoteServiceServlet implements UserService {

    private static final Logger       LOGGER      = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Display      display     = Display.initialize("view/user.xml");

    /** {@inheritDoc} */
    @Override
    public List<TableData> getData(String userEmail) throws IOException {
        userEmail = "gennady.azarenkov@exoplatform.com"; // TODO empty

        Map<String, String> context = Utils.newContext();
        context.put(MetricFilter.FILTER_USER.name(), userEmail);
        context.put(MetricParameter.ALIAS.getName(), userEmail);
        context.put(MetricParameter.FROM_DATE.getName(), MetricParameter.FROM_DATE.getDefaultValue());
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());

        try {
            return display.retrieveData(context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new ArrayList<TableData>();
    }
}
