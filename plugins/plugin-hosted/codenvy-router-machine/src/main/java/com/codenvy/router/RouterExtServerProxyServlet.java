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
package com.codenvy.router;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.proxy.MachineExtensionProxyServlet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Makes {@link MachineExtensionProxyServlet} use direct urls of servers in machine instead of urls to router
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RouterExtServerProxyServlet extends MachineExtensionProxyServlet {
    @Inject
    public RouterExtServerProxyServlet(@Named("machine.extension.api_port") String wsAgentPort,
                                       MachineManager machineManager) {
        super(wsAgentPort, new RouterMachineManager((RouterMachineManager)machineManager) {

            @Override
            public MachineImpl getDevMachine(String workspaceId) throws NotFoundException, MachineException {
                return getDevMachineWithDirectServersUrls(workspaceId);
            }
        });
    }
}
