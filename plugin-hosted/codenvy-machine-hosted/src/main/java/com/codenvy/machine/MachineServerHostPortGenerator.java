/*
 *  [2012] - [2016] Codenvy, S.A.
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

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Machine server host, port generator that uses predefined URL template that allows to customize machine's server url.
 * <p>
 * Can be used to enable https proxying for terminal or ws agent.
 *
 * @author Alexander Garagatyi
 */
public class MachineServerHostPortGenerator {
    private final String httpsServerHostPortTemplate;
    private final String httpServerHostPortTemplate;

    @Inject
    public MachineServerHostPortGenerator(@Named("machine.docker.https_server_host_port_template") String httpsServerHostPortTemplate,
                                          @Named("machine.docker.http_server_host_port_template") String httpServerHostPortTemplate) {
        this.httpsServerHostPortTemplate = httpsServerHostPortTemplate;
        this.httpServerHostPortTemplate = httpServerHostPortTemplate;
    }

    /**
     * Generates host:port address of machine server.
     * <p/>
     * Template must follow java.util.Formatter rules.
     * <br>Template accepts such parameters:
     * <ol>
     *     <li>Server reference</li>
     *     <li>Protocol of server</li>
     *     <li>Origin host of server</li>
     *     <li>Origin port of server</li>
     * </ol>
     * Example:
     * <ul>
     *     <li>ref: myref</li>
     *     <li>host: some.host.cloud-server.com</li>
     *     <li>port: 8080</li>
     * </ul>
     * Pattern: %1$s%4$s-smth-%3$s
     * <br>Result: myref8080-smth-some.host.cloud-server.com
     *
     * @param ref
     *         reference of machine server
     * @param protocol
     *         machine server protocol
     * @param host
     *         origin machine server host
     * @param port
     *         origin machine server port
     */
    public String generate(String ref, String protocol, String host, int port) {
        String newHostPort;
        if ("https".equals(protocol)) {
            newHostPort = String.format(httpsServerHostPortTemplate, ref, protocol, host, port);
        } else if ("http".equals(protocol)) {
            newHostPort = String.format(httpServerHostPortTemplate, ref, protocol, host, port);
        } else {
            newHostPort = host + ":" + port;
        }
        return newHostPort;
    }
}
