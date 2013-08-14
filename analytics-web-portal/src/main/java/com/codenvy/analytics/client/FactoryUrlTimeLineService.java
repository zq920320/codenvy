/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;
import java.util.Map;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("TimeLine")
public interface FactoryUrlTimeLineService extends RemoteService {

    /**
     * Retrieves data from server.
     */
    List<TableData> getData(TimeUnit timeUnit, Map<String, String> filterContext);
}
