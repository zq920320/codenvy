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

import java.util.Objects;

/**
 * Describes routing rule for machine server.
 *
 * @author Alexander Garagatyi
 */
public class RoutingRule {
    private String       host;
    private int          externalPort;
    private int          exposedPort;
    private String       uri;

    public RoutingRule() {
    }

    public RoutingRule(String host, int externalPort, int exposedPort, String uri) {
        this.host = host;
        this.externalPort = externalPort;
        this.exposedPort = exposedPort;
        this.uri = uri;
    }

    public String getHost() {
        return this.host;
    }

    public int getExternalPort() {
        return this.externalPort;
    }

    public int getExposedPort() {
        return exposedPort;
    }

    public String getUri() {
        return this.uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutingRule)) return false;
        RoutingRule that = (RoutingRule)o;
        return externalPort == that.externalPort &&
               Objects.equals(exposedPort, that.exposedPort) &&
               Objects.equals(host, that.host) &&
               Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, externalPort, exposedPort, uri);
    }
}
