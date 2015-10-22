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

import nats.client.Nats;
import nats.client.NatsConnector;

import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonNameConventions;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Sends routing rules to gorouter over nats server
 *
 * @author Alexander Garagatyi
 */
@Singleton // should be eager
public class RouterRulesRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RouterRulesRegistry.class);

    private final NatsConnector                                natsConnector;
    private final ConcurrentHashMap<String, List<RoutingRule>> routingRules;

    private Nats nats;

    @Inject
    public RouterRulesRegistry(@Named("router.nats.url") String natsUrl) {
        this.routingRules = new ConcurrentHashMap<>();

        this.natsConnector = new NatsConnector().automaticReconnect(true)
                                                .addHost(natsUrl)
                                                .automaticReconnect(true)
                                                .reconnectWaitTime(30, TimeUnit.SECONDS);
                                                //.addConnectionStateListener()
                                                // TODO listen connection state. If connection failed do not publish, queue instead
    }

    public void addRule(String machineId, RoutingRule routingRule) {
        routingRules.putIfAbsent(machineId, new LinkedList<>());

        routingRules.get(machineId).add(routingRule);

        try {
            checkNatsConnection();

            sendRule(routingRule.getHost(), routingRule.getExternalPort(), routingRule.getUri());
        } catch (Exception ignore) {
        }
    }

    public void removeRules(String machineId) {
        routingRules.remove(machineId);
        // TODO should we stop routing manually?
    }

    public List<RoutingRule> getRules(String machineId) {
        return Collections
                .unmodifiableList(routingRules.containsKey(machineId) ? routingRules.get(machineId) : Collections.<RoutingRule>emptyList());
    }

    @ScheduleDelay(initialDelay = 1,
            delay = 1,
            unit = TimeUnit.MINUTES)
    private void registerRoutingRules() throws Exception {
        checkNatsConnection();

        for (Map.Entry<String, List<RoutingRule>> machineRoutingRules : routingRules.entrySet()) {
            for (RoutingRule routingRule : machineRoutingRules.getValue()) {
                sendRule(routingRule.getHost(), routingRule.getExternalPort(), routingRule.getUri());
            }
        }
    }

    private void checkNatsConnection() throws Exception {
        if (nats == null) {
            nats = natsConnector.connect();
        } else if (!nats.isConnected()) {
            // for testing purposes
            LOG.warn("Nats client has lost connection");
        }
    }

    private void sendRule(String backendHost, int backendPort, String routingUri) {
        GorouterRule gorouterRule = new GorouterRule(backendHost,
                                                     backendPort,
                                                     Collections.singletonList(routingUri),
                                                     null,
                                                     null,
                                                     null,
                                                     null);
        nats.publish("router.register",
                     JsonHelper.toJson(gorouterRule, JsonNameConventions.CAMEL_UNDERSCORE),
                     "machine.replies");
    }
}
