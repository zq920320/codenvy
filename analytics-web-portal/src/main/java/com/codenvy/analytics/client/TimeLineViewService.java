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
import java.util.List;
import java.util.Map;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("timeLineView")
public interface TimeLineViewService extends RemoteService {

    /**
     * Returns rows for timeline view.
     */
    List<TimeLineViewData> getViews(TimeUnit timeUnit, Map<String, String> filters) throws IOException;
}
