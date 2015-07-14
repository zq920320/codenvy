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
package com.codenvy.router;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Describes routing rule for cloudfoundry gorouter.
 * <p>https://github.com/cloudfoundry/gorouter
 *
 * @author Alexander Garagatyi
 */
public class GorouterRule {
    private String              host;
    private int                 port;
    private List<String>        uris;
    private Map<String, String> tags;
    private String              app;
    private String              staleThresholdInSeconds;
    private String              privateInstanceId;

    public GorouterRule() {
    }

    public GorouterRule(String host, int port, List<String> uris, Map<String, String> tags, String app, String staleThresholdInSeconds,
                        String privateInstanceId) {
        this.host = host;
        this.port = port;
        this.uris = uris;
        this.tags = tags;
        this.app = app;
        this.staleThresholdInSeconds = staleThresholdInSeconds;
        this.privateInstanceId = privateInstanceId;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public List<String> getUris() {
        return this.uris == null ? this.uris = new LinkedList<>() : this.uris;
    }

    public String getApp() {
        return this.app;
    }

    public Map<String, String> getTags() {
        return this.tags == null ? this.tags = new HashMap<>() : this.tags;
    }

    public String getStaleThresholdInSeconds() {
        return this.staleThresholdInSeconds;
    }

    public String getPrivateInstanceId() {
        return this.privateInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GorouterRule)) return false;
        GorouterRule that = (GorouterRule)o;
        return port == that.port &&
               Objects.equals(host, that.host) &&
               Objects.equals(getUris(), that.getUris()) &&
               Objects.equals(getTags(), that.getTags()) &&
               Objects.equals(app, that.app) &&
               Objects.equals(staleThresholdInSeconds, that.staleThresholdInSeconds) &&
               Objects.equals(privateInstanceId, that.privateInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, getUris(), getTags(), app, staleThresholdInSeconds, privateInstanceId);
    }
}
