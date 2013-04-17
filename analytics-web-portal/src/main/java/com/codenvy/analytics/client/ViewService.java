package com.codenvy.analytics.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("view")
public interface ViewService extends RemoteService {
    List<List<String>> getTimeLineView(Date date) throws IOException;
}
