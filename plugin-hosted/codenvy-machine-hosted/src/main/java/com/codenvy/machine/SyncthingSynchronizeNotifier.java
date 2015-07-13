/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.machine;

import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Notify Syncthing about changes in project's files on VFS
 *
 * @author Alexander Garagatyi
 */
public class SyncthingSynchronizeNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(SyncthingSynchronizeNotifier.class);

    private final String project;
    private final String workspace;
    private final int    port;
    private final String apiToken;

    public SyncthingSynchronizeNotifier(String workspace, String projectPath, int port, String apiToken) {
        this.project = projectPath;
        this.workspace = workspace;
        this.port = port;
        this.apiToken = apiToken;
    }

    public String getProject() {
        return project;
    }

    public String getWorkspaceId() {
        return workspace;
    }

    public void notifySynchronizer(String path) {
        try {
            final UriBuilder uri = UriBuilder.fromUri("http://127.0.0.1/rest/scan")
                                             .port(port)
                                             .queryParam("folder", "default")
                                             .queryParam("sub", path);

            final HttpURLConnection conn = (HttpURLConnection)uri.build().toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.addRequestProperty("X-API-Key", apiToken);
            conn.connect();

            if (conn.getResponseCode() != 200) {
                LOG.error(IoUtil.readAndCloseQuietly(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncthingSynchronizeNotifier that = (SyncthingSynchronizeNotifier)o;

        if (port != that.port) return false;
        if (apiToken != null ? !apiToken.equals(that.apiToken) : that.apiToken != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (workspace != null ? !workspace.equals(that.workspace) : that.workspace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (workspace != null ? workspace.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (apiToken != null ? apiToken.hashCode() : 0);
        return result;
    }
}
