/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/** The client side stub for the RPC service. */

@RemoteServiceRelativePath("timeLineView")
public interface TimeLineViewService extends RemoteService {

    /**
     * Returns rows for timeline view.
     */
    List<TimeLineViewData> getViews(Date date, TimeUnit timeUnit) throws IOException;
}
