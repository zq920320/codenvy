/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("SingupAnalysisView")
public interface SingupAnalysisViewService extends RemoteService {

    /**
     * Returns data for view.
     */
    List<TimeLineViewData> getData();
}
