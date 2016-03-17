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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Listens events about machines and adds/removes machine routing rules to routing registry
 *
 * @author Alexander Garagatyi
 */
@Singleton // should be eager
public class RouterListener implements EventSubscriber<MachineStatusEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RouterListener.class);

    private final EventService        eventService;
    private final MachineRegistry     registry;
    private final RouterRulesRegistry routerRulesRegistry;

    /*
    String that represents external uri of the machine server which is routed to machine.
    String should be used with String.format where arguments are:
    1. port in machine,
    2. real address where machine is accessible
    3. external port that is bound to port in machine,
    4. id of the machine

    Examples:
    Pattern                     Result uri
    %4$s-%1$s.ide-machine.com - > machineId-exposedPort.ide-machine.com
    %2$s:%3$s                 - > aws-12334.amazon.com:31598
     */
    private final String machineServerRoutingPattern;

    @Inject
    public RouterListener(EventService eventService,
                          MachineRegistry registry,
                          RouterRulesRegistry routerRulesRegistry,
                          @Named("machine.router.routing_pattern") String routingPattern) {
        this.eventService = eventService;
        this.registry = registry;
        this.routerRulesRegistry = routerRulesRegistry;
        this.machineServerRoutingPattern = routingPattern;
    }

    @Override
    public void onEvent(MachineStatusEvent event) {
        switch (event.getEventType()) {
            case RUNNING:
                addRouting(event.getMachineId());
                break;
            case DESTROYING:
                removeRouting(event.getMachineId());
                break;
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    private void addRouting(String machineId) {
        try {
            // this class require appropriate implementation of Instance
            final Instance machine = registry.getInstance(machineId);
            machine.getRuntime()
                   .getServers()
                   .entrySet()
                   .stream()
                   .filter(serverInMachine -> serverInMachine.getKey().endsWith("/tcp"))
                   .forEach(serverInMachine -> {
                       final String[] serverHostPort = serverInMachine.getValue().getAddress().split(":", 2);
                       String exposedPortWithoutProtocol = serverInMachine.getKey().substring(0, serverInMachine.getKey().length() - 4);
                       routerRulesRegistry.addRule(machineId, new RoutingRule(serverHostPort[0],
                                                                              Integer.parseInt(serverHostPort[1]),
                                                                              Integer.parseInt(exposedPortWithoutProtocol),
                                                                              String.format(machineServerRoutingPattern,
                                                                                            exposedPortWithoutProtocol,
                                                                                            serverHostPort[0],
                                                                                            serverHostPort[1],
                                                                                            machineId)));
                   });
            // TODO add url with machine name, 80 port, terminal
        } catch (NotFoundException | MachineException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private void removeRouting(String machineId) {
        // remove from routing updates map, so will halt routing for machines after stale threshold configured in router
        routerRulesRegistry.removeRules(machineId);
    }
}
