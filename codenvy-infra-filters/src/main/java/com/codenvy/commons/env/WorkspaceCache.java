/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.commons.env;

import com.codenvy.api.workspace.shared.dto.Workspace;
import com.codenvy.commons.lang.ExpirableCache;


/**
 * Cache that maps workspace to some information of that ws.
 * Extends expirable cache to provide thread-safety.
 */
public class WorkspaceCache extends ExpirableCache<String, Workspace> {
    public WorkspaceCache(long expiredAfter, int cacheSize) {
        super(expiredAfter, cacheSize);
    }

    @Override
    public synchronized void put(String key, Workspace value) {
        super.put(key, value);
    }

    @Override
    public synchronized Workspace get(String key) {
        return super.get(key);
    }
}
