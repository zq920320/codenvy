/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("Analysis")
public interface AnalysisService extends RemoteService {

    /**
     * Retrieves data from server.
     */
    List<TableData> getData();
}
